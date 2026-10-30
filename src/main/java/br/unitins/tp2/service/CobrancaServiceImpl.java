package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.tp2.dto.CobrancaResponseDTO;
import br.unitins.tp2.dto.ExecucaoCobrancaResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.ExecucaoCobrancaResponseMapper;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.ExecucaoCobranca;
import br.unitins.tp2.model.MudancaAssinatura;
import br.unitins.tp2.model.OrigemExecucaoCobranca;
import br.unitins.tp2.model.OrigemMudancaAssinatura;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusExecucaoCobranca;
import br.unitins.tp2.model.TipoMudancaAssinatura;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.ExecucaoCobrancaRepository;
import br.unitins.tp2.repository.MudancaAssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.util.RegrasCobranca;
import br.unitins.tp2.util.RetratoAuditoria;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Cobranca das assinaturas (RN16). Para cada assinatura com a proxima cobranca vencida:
// 1. conta desativada nao e cobrada: volta ao Gratuito;
// 2. a troca agendada (plano mais barato ou cancelamento), se houver, passa a valer;
// 3. plano sem custo: deixa de ter cobranca. Plano pago: registra o Pagamento (simulado e ja
//    aprovado, RN15) e o periodo avanca um ciclo. Se varios ciclos venceram, cobra um por um.
// Cada assinatura vai na propria transacao: a que falhar vira um erro da execucao e as outras seguem.
// Toda execucao (automatica ou manual) fica no historico, gravada ao final em transacao propria.
@ApplicationScoped
public class CobrancaServiceImpl implements CobrancaService {

    private static final Logger LOG = Logger.getLogger(CobrancaServiceImpl.class);
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String AGENDADOR = "Agendador diário (03:00)";

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    PlanoRepository planoRepository;

    @Inject
    MudancaAssinaturaRepository mudancaRepository;

    @Inject
    ExecucaoCobrancaRepository execucaoRepository;

    // o que a cobranca fez numa assinatura
    private record Resultado(int pagamentos, BigDecimal valor, int trocas, int gratuitos) {
    }

    @Scheduled(cron = "{fincontrol.cobranca.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cobrarDiariamente() {
        CobrancaResponseDTO resultado = executar(LocalDate.now(), OrigemExecucaoCobranca.AUTOMATICA, null);
        LOG.infof("Cobranca de %s (%s): %d assinatura(s), %d pagamento(s), R$ %s", resultado.data(),
                resultado.status().getNome(), resultado.assinaturasProcessadas(), resultado.pagamentosGerados(),
                resultado.valorTotal());
    }

    @Override
    public CobrancaResponseDTO executar(LocalDate data, OrigemExecucaoCobranca origem, AuditoriaService.Admin admin) {
        LocalDateTime inicio = LocalDateTime.now();
        long relogio = System.nanoTime();
        int assinaturas = 0, pagamentos = 0, trocas = 0, gratuitos = 0;
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        List<ExecucaoCobrancaResponseDTO.Erro> erros = new ArrayList<>();
        String falhaGeral = null;

        try {
            List<Long> ids = QuarkusTransaction.requiringNew().call(() -> assinaturaRepository.findIdsComCobrancaAte(data));
            for (Long id : ids) {
                assinaturas++;
                try {
                    Resultado r = QuarkusTransaction.requiringNew().call(() -> cobrar(id, data));
                    pagamentos += r.pagamentos();
                    total = total.add(r.valor());
                    trocas += r.trocas();
                    gratuitos += r.gratuitos();
                } catch (RuntimeException e) {
                    LOG.errorf(e, "Cobranca da assinatura %d falhou; as demais seguem", id);
                    erros.add(new ExecucaoCobrancaResponseDTO.Erro(id, usuarioDa(id), mensagem(e)));
                }
            }
        } catch (RuntimeException e) {
            LOG.errorf(e, "A cobranca de %s falhou", data);
            falhaGeral = mensagem(e);
        }

        StatusExecucaoCobranca status = RegrasCobranca.status(assinaturas, erros.size(), falhaGeral != null);
        ExecucaoCobranca execucao = new ExecucaoCobranca();
        execucao.setDataReferencia(data);
        execucao.setInicio(inicio);
        execucao.setFim(LocalDateTime.now());
        execucao.setDuracaoMs((System.nanoTime() - relogio) / 1_000_000);
        execucao.setOrigem(origem);
        execucao.setStatus(status);
        execucao.setAdminKeycloakId(admin == null ? null : admin.keycloakId());
        execucao.setDisparadoPor(admin == null ? AGENDADOR
                : admin.nome() + (admin.email() == null ? "" : " (" + admin.email() + ")"));
        execucao.setAssinaturasProcessadas(assinaturas);
        execucao.setPagamentosGerados(pagamentos);
        execucao.setValorTotal(total);
        execucao.setTrocasAplicadas(trocas);
        execucao.setVoltasAoGratuito(gratuitos);
        execucao.setMensagemErro(falhaGeral);
        execucao.setErros(paraJson(erros));
        QuarkusTransaction.requiringNew().run(() -> execucaoRepository.persist(execucao));

        return new CobrancaResponseDTO(data, assinaturas, pagamentos, total, trocas, gratuitos, execucao.getId(), status);
    }

    // uma assinatura, na transacao aberta por executar()
    private Resultado cobrar(Long id, LocalDate data) {
        Assinatura assinatura = assinaturaRepository.findById(id);
        int pagamentos = 0, trocas = 0, gratuitos = 0;
        BigDecimal total = BigDecimal.ZERO.setScale(2);

        while (assinatura.getDataProximaCobranca() != null && !assinatura.getDataProximaCobranca().isAfter(data)) {
            LocalDate dia = assinatura.getDataProximaCobranca();

            if (!assinatura.getUsuario().getAtivo()) {
                Plano gratuito = buscarGratuitoOuFalhar();
                MudancaAssinatura volta = AssinaturaServiceImpl.mudancaDoVigente(assinatura,
                        TipoMudancaAssinatura.VOLTA_GRATUITO_CONTA_DESATIVADA, OrigemMudancaAssinatura.COBRANCA, dia);
                volta.definirNovo(gratuito, Periodicidade.MENSAL, BigDecimal.ZERO);
                mudancaRepository.persist(volta);
                AssinaturaServiceImpl.aplicarGratuito(assinatura, gratuito, dia);
                gratuitos++;
                break;
            }

            MudancaAssinatura troca = null;
            if (assinatura.isTrocaAgendada()) {
                TipoMudancaAssinatura tipo = assinatura.getProximoPlano().isGratuito()
                        ? TipoMudancaAssinatura.CANCELAMENTO_APLICADO : TipoMudancaAssinatura.DOWNGRADE_APLICADO;
                troca = AssinaturaServiceImpl.mudancaDoVigente(assinatura, tipo, OrigemMudancaAssinatura.COBRANCA, dia);
                aplicarTrocaAgendada(assinatura);
                trocas++;
            }

            // o preco e recalculado na cobranca: um reajuste do plano vale a partir do proximo ciclo
            BigDecimal valor = assinatura.getPlano().calcularValor(assinatura.getPeriodicidade(),
                    assinatura.getCupom());
            assinatura.setValor(valor);
            if (troca != null) {
                troca.definirNovo(assinatura.getPlano(), assinatura.getPeriodicidade(), valor);
                mudancaRepository.persist(troca);
            }

            if (valor.signum() == 0) {
                gratuitos += assinatura.getPlano().isGratuito() ? 1 : 0;
                assinatura.setDataInicioPeriodo(dia);
                assinatura.setDataProximaCobranca(null);
                assinatura.setValorPeriodo(valor);
                break;
            }

            pagamentoRepository.persist(AssinaturaServiceImpl.novoPagamento(assinatura, dia));
            pagamentos++;
            total = total.add(valor);
            assinatura.setDataInicioPeriodo(dia);
            assinatura.setDataProximaCobranca(assinatura.getPeriodicidade().somarCiclo(dia));
            assinatura.setValorPeriodo(valor);
        }
        return new Resultado(pagamentos, total, trocas, gratuitos);
    }

    @Override
    public List<ExecucaoCobranca> buscarExecucoes(LocalDate inicio, LocalDate fim, OrigemExecucaoCobranca origem,
            StatusExecucaoCobranca status, int page, int pageSize) {
        validarPeriodo(inicio, fim);
        return execucaoRepository.findByFiltros(inicio, fim, origem, status).page(page, pageSize).list();
    }

    @Override
    public long contarExecucoes(LocalDate inicio, LocalDate fim, OrigemExecucaoCobranca origem,
            StatusExecucaoCobranca status) {
        return execucaoRepository.findByFiltros(inicio, fim, origem, status).count();
    }

    @Override
    public ExecucaoCobranca findExecucao(long id) {
        ExecucaoCobranca execucao = execucaoRepository.findById(id);
        if (execucao == null)
            throw new ResourceNotFoundException("Execução da cobrança não encontrada.");
        return execucao;
    }

    // so ate ontem: a de hoje pode ainda nao ter chegado na hora do agendador
    @Override
    public List<LocalDate> diasSemAutomatica(LocalDate inicio, LocalDate fim) {
        validarPeriodo(inicio, fim);
        LocalDate ontem = LocalDate.now().minusDays(1);
        LocalDate ate = fim.isAfter(ontem) ? ontem : fim;
        if (inicio.isAfter(ate))
            return List.of();
        return RegrasCobranca.diasSemExecucao(inicio, ate, execucaoRepository.findDiasComAutomatica(inicio, ate));
    }

    @Override
    public ExecucaoCobrancaResponseDTO.Resumo resumo(int dias) {
        LocalDate ontem = LocalDate.now().minusDays(1);
        LocalDate inicio = LocalDate.now().minusDays(dias);
        ExecucaoCobranca deOntem = execucaoRepository.findUltimaAutomatica(ontem);
        return new ExecucaoCobrancaResponseDTO.Resumo(ontem,
                deOntem == null ? null : ExecucaoCobrancaResponseMapper.toResponse(deOntem),
                execucaoRepository.countFalhasDesde(inicio), diasSemAutomatica(inicio, ontem), inicio);
    }

    private Plano buscarGratuitoOuFalhar() {
        Plano gratuito = planoRepository.findByNomeExato(Plano.NOME_GRATUITO);
        if (gratuito == null)
            throw ValidationException.of("idPlano", "Plano \"" + Plano.NOME_GRATUITO + "\" não encontrado.");
        return gratuito;
    }

    private void aplicarTrocaAgendada(Assinatura assinatura) {
        assinatura.setPlano(assinatura.getProximoPlano());
        assinatura.setPeriodicidade(assinatura.getProximaPeriodicidade());
        assinatura.setCupom(assinatura.getProximoCupom());
        assinatura.setFormaPagamento(assinatura.getProximaFormaPagamento());
        assinatura.limparTrocaAgendada();
    }

    // "Nome (e-mail)" do dono da assinatura que falhou, para o detalhe da execucao
    private String usuarioDa(Long idAssinatura) {
        try {
            return QuarkusTransaction.requiringNew().call(() -> {
                Assinatura assinatura = assinaturaRepository.findById(idAssinatura);
                return assinatura == null ? null : RetratoAuditoria.descricao(assinatura.getUsuario());
            });
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String mensagem(Throwable e) {
        Throwable causa = e;
        while (causa.getCause() != null && causa.getCause() != causa)
            causa = causa.getCause();
        String texto = causa.getMessage() != null ? causa.getMessage() : causa.getClass().getSimpleName();
        return texto.length() > 300 ? texto.substring(0, 300) : texto;
    }

    private static String paraJson(List<ExecucaoCobrancaResponseDTO.Erro> erros) {
        try {
            return JSON.writeValueAsString(erros);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw ValidationException.of("inicio", "A data inicial deve ser anterior ou igual à final.");
    }
}
