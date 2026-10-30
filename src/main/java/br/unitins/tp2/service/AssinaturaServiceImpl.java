package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

import br.unitins.tp2.dto.ObservacaoAssinaturaDTO;
import br.unitins.tp2.dto.SimulacaoRecusaDTO;
import br.unitins.tp2.dto.TrocaPlanoDTO;
import br.unitins.tp2.dto.TrocaPlanoResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.AssinaturaResponseMapper;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.EfeitoTroca;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.MudancaAssinatura;
import br.unitins.tp2.model.OrigemMudancaAssinatura;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.model.TipoMudancaAssinatura;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.CupomRepository;
import br.unitins.tp2.repository.MudancaAssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.RegrasCupom;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// Uma assinatura por usuario (RN16): ela nasce no Gratuito e depois so muda de plano.
//
// Troca de plano no estilo da Netflix:
// - plano mais caro (preco por dia maior): vale na hora. Nada e cobrado agora: o saldo nao usado
//   do periodo atual vira dias do plano novo e a proxima cobranca fica mais cedo. Saindo do
//   Gratuito nao ha saldo, entao o primeiro ciclo e cobrado na hora;
// - plano mais barato ou cancelamento: fica agendado para a proxima cobranca e os recursos atuais
//   continuam ate la. Uma nova troca antes da cobranca substitui a agendada.
@ApplicationScoped
public class AssinaturaServiceImpl implements AssinaturaService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PlanoRepository planoRepository;

    @Inject
    CupomRepository cupomRepository;

    @Inject
    MudancaAssinaturaRepository mudancaRepository;

    @Inject
    AuditoriaService auditoria;

    // plano, periodicidade, cupom e forma ja validados, com o valor de cada cobranca
    private record Escolha(Plano plano, Periodicidade periodicidade, Cupom cupom, FormaPagamento forma,
            BigDecimal valor) {
    }

    private record Calculo(EfeitoTroca efeito, BigDecimal credito, long diasCobertos, BigDecimal valorCobradoAgora,
            LocalDate dataProximaCobranca, BigDecimal valorProximaCobranca) {
    }

    // Sem o plano Gratuito configurado, o cadastro falha (e o usuario do Keycloak e desfeito
    // pelo UsuarioServiceImpl.registrar).
    @Override
    @Transactional
    public Assinatura criarGratuita(Usuario usuario) {
        Plano gratuito = buscarGratuitoOuFalhar();
        LocalDate hoje = LocalDate.now();

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setDataInicio(hoje);
        assinatura.setObservacao("Plano gratuito aplicado no cadastro");
        aplicarGratuito(assinatura, gratuito, hoje);
        assinaturaRepository.persist(assinatura);

        MudancaAssinatura cadastro = novaMudanca(assinatura, TipoMudancaAssinatura.CADASTRO,
                OrigemMudancaAssinatura.CADASTRO, hoje);
        cadastro.definirNovo(gratuito, Periodicidade.MENSAL, ZERO);
        mudancaRepository.persist(cadastro);
        return assinatura;
    }

    @Override
    public Assinatura buscarDoUsuario(long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario);
        if (usuario == null)
            throw new ResourceNotFoundException("Usuário não encontrado.");

        Assinatura assinatura = assinaturaRepository.findByUsuario(idUsuario);
        if (assinatura == null)
            throw new ResourceNotFoundException("Este usuário não tem assinatura.");

        return assinatura;
    }

    // so calcula: nada muda no banco
    @Override
    public TrocaPlanoResponseDTO simularTroca(long idUsuario, TrocaPlanoDTO troca) {
        Assinatura assinatura = buscarParaTrocaOuFalhar(idUsuario);
        Escolha escolha = resolverEscolha(assinatura, troca);
        Calculo calculo = calcular(assinatura, escolha, LocalDate.now());

        return toResponse(calculo, assinatura);
    }

    @Override
    @Transactional
    public TrocaPlanoResponseDTO trocarPlano(long idUsuario, TrocaPlanoDTO troca) {
        Assinatura assinatura = buscarParaTrocaOuFalhar(idUsuario);
        Escolha escolha = resolverEscolha(assinatura, troca);
        LocalDate hoje = LocalDate.now();
        Calculo calculo = calcular(assinatura, escolha, hoje);

        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        aplicar(assinatura, escolha, calculo, hoje);
        auditar(calculo.efeito() == EfeitoTroca.DESFAZ_AGENDAMENTO ? AcaoAuditoria.DESFAZER_TROCA
                : AcaoAuditoria.TROCA_PLANO, assinatura, antes);

        return toResponse(calculo, assinatura);
    }

    @Override
    @Transactional
    public TrocaPlanoResponseDTO cancelarPlanoPago(long idUsuario) {
        Assinatura assinatura = buscarDeContaAtivaOuFalhar(idUsuario);
        Plano gratuito = buscarGratuitoOuFalhar();

        if (assinatura.getDataProximaCobranca() == null)
            throw ValidationException.of("idPlano", "O usuário já está no plano Gratuito.");
        if (assinatura.getStatus() == StatusAssinatura.PENDENTE)
            return cancelarComRecusaPendente(assinatura, gratuito);
        if (assinatura.isTrocaAgendada() && assinatura.getProximoPlano().getId().equals(gratuito.getId()))
            throw ValidationException.of("idPlano", "A volta ao plano Gratuito já está agendada.");

        Escolha escolha = new Escolha(gratuito, Periodicidade.MENSAL, null, FormaPagamento.NENHUMA, ZERO);
        Calculo calculo = calcular(assinatura, escolha, LocalDate.now());
        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        aplicar(assinatura, escolha, calculo, LocalDate.now());
        auditar(AcaoAuditoria.CANCELAMENTO_PLANO, assinatura, antes);

        return toResponse(calculo, assinatura);
    }

    // Com cobranca recusada pendente nao ha periodo pago a esperar: volta ao Gratuito na hora e a
    // cobranca recusada deixa de ser tentada.
    private TrocaPlanoResponseDTO cancelarComRecusaPendente(Assinatura assinatura, Plano gratuito) {
        LocalDate hoje = LocalDate.now();
        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        MudancaAssinatura mudanca = mudancaDoVigente(assinatura, TipoMudancaAssinatura.CANCELAMENTO_APLICADO,
                OrigemMudancaAssinatura.ADMIN, hoje);
        mudanca.definirNovo(gratuito, Periodicidade.MENSAL, ZERO);
        mudanca.setDetalhe("Cancelado com pagamento recusado pendente: volta ao Gratuito na hora, sem nova tentativa.");
        mudancaRepository.persist(mudanca);
        aplicarGratuito(assinatura, gratuito, hoje);
        auditar(AcaoAuditoria.CANCELAMENTO_PLANO, assinatura, antes);

        return toResponse(new Calculo(EfeitoTroca.IMEDIATO, ZERO, 0, ZERO, null, ZERO), assinatura);
    }

    @Override
    @Transactional
    public Assinatura desfazerTroca(long idUsuario) {
        Assinatura assinatura = buscarDoUsuario(idUsuario);
        if (!assinatura.isTrocaAgendada())
            throw ValidationException.of("idPlano", "Não há troca de plano agendada.");

        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        mudancaRepository.persist(registroDoDesfazer(assinatura, LocalDate.now()));
        assinatura.limparTrocaAgendada();
        auditar(AcaoAuditoria.DESFAZER_TROCA, assinatura, antes);
        return assinatura;
    }

    @Override
    @Transactional
    public Assinatura salvarObservacao(long idUsuario, ObservacaoAssinaturaDTO dto) {
        Assinatura assinatura = buscarDoUsuario(idUsuario);
        String observacao = dto.observacao();
        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        assinatura.setObservacao(observacao == null || observacao.isBlank() ? null : observacao.trim());
        auditar(AcaoAuditoria.OBSERVACAO_ASSINATURA, assinatura, antes);

        return assinatura;
    }

    // A cobranca (CobrancaServiceImpl) recusa enquanto estiver ligada. Ligar so faz sentido com uma
    // cobranca pela frente; desligar sempre pode (a proxima tentativa passa).
    @Override
    @Transactional
    public Assinatura definirSimulacaoRecusa(long idUsuario, SimulacaoRecusaDTO simulacao) {
        Assinatura assinatura = buscarDoUsuario(idUsuario);
        if (simulacao.ligada() && assinatura.getDataProximaCobranca() == null)
            throw ValidationException.of("ligada", "Esta assinatura não tem cobrança (plano sem custo): "
                    + "não há pagamento para recusar.");
        if (simulacao.ligada().equals(assinatura.getSimularRecusa()))
            return assinatura;

        Map<String, String> antes = RetratoAuditoria.de(assinatura);
        assinatura.setSimularRecusa(simulacao.ligada());
        auditar(AcaoAuditoria.SIMULACAO_RECUSA, assinatura, antes);
        return assinatura;
    }

    // ===== calculo da troca (RN16) =====

    private Calculo calcular(Assinatura atual, Escolha nova, LocalDate hoje) {
        if (isPlanoVigente(atual, nova)) {
            if (atual.isTrocaAgendada())
                return new Calculo(EfeitoTroca.DESFAZ_AGENDAMENTO, ZERO, 0, ZERO,
                        atual.getDataProximaCobranca(), atual.getValor());
            throw ValidationException.of("idPlano", "Este já é o plano atual do usuário.");
        }

        boolean atualSemCobranca = atual.getDataProximaCobranca() == null;

        // plano sem custo: do Gratuito para outro sem custo vale na hora; de um pago, so na proxima cobranca
        if (nova.valor().signum() == 0) {
            if (atualSemCobranca)
                return new Calculo(EfeitoTroca.IMEDIATO, ZERO, 0, ZERO, null, ZERO);
            return new Calculo(EfeitoTroca.AGENDADO, ZERO, 0, ZERO, atual.getDataProximaCobranca(), ZERO);
        }

        // saindo do Gratuito nao ha saldo: o primeiro ciclo do plano novo e cobrado agora
        if (atualSemCobranca)
            return cobrarCicloHoje(nova, ZERO, hoje);

        BigDecimal diarioAtual = precoPorDia(atual.getValor(), atual.getPeriodicidade(), hoje);
        BigDecimal diarioNovo = precoPorDia(nova.valor(), nova.periodicidade(), hoje);

        if (diarioNovo.compareTo(diarioAtual) <= 0)
            return new Calculo(EfeitoTroca.AGENDADO, ZERO, 0, ZERO, atual.getDataProximaCobranca(), nova.valor());

        // mais caro: o saldo nao usado do periodo vira dias do plano novo
        BigDecimal credito = creditoRestante(atual, hoje);
        long diasCobertos = credito.divide(diarioNovo, 0, RoundingMode.FLOOR).longValue();
        if (diasCobertos <= 0)
            return cobrarCicloHoje(nova, credito, hoje);

        return new Calculo(EfeitoTroca.IMEDIATO, credito, diasCobertos, ZERO, hoje.plusDays(diasCobertos),
                nova.valor());
    }

    private Calculo cobrarCicloHoje(Escolha nova, BigDecimal credito, LocalDate hoje) {
        return new Calculo(EfeitoTroca.IMEDIATO, credito, 0, nova.valor(), nova.periodicidade().somarCiclo(hoje),
                nova.valor());
    }

    // parte do valor do periodo atual que ainda nao foi usada (proporcional aos dias que faltam)
    private BigDecimal creditoRestante(Assinatura atual, LocalDate hoje) {
        long diasPeriodo = ChronoUnit.DAYS.between(atual.getDataInicioPeriodo(), atual.getDataProximaCobranca());
        if (diasPeriodo <= 0)
            return ZERO;

        long diasRestantes = ChronoUnit.DAYS.between(hoje, atual.getDataProximaCobranca());
        diasRestantes = Math.max(0, Math.min(diasRestantes, diasPeriodo));

        return atual.getValorPeriodo()
                .multiply(BigDecimal.valueOf(diasRestantes))
                .divide(BigDecimal.valueOf(diasPeriodo), 2, RoundingMode.HALF_UP);
    }

    // valor de um ciclo dividido pelos dias de um ciclo a partir de hoje (mensal ~30, anual ~365)
    private BigDecimal precoPorDia(BigDecimal valor, Periodicidade periodicidade, LocalDate hoje) {
        long dias = ChronoUnit.DAYS.between(hoje, periodicidade.somarCiclo(hoje));
        return valor.divide(BigDecimal.valueOf(dias), 6, RoundingMode.HALF_UP);
    }

    private void aplicar(Assinatura assinatura, Escolha nova, Calculo calculo, LocalDate hoje) {
        mudancaRepository.persist(registroDaTroca(assinatura, nova, calculo, hoje));

        switch (calculo.efeito()) {
            case DESFAZ_AGENDAMENTO -> assinatura.limparTrocaAgendada();
            case AGENDADO -> {
                assinatura.setProximoPlano(nova.plano());
                assinatura.setProximaPeriodicidade(nova.periodicidade());
                assinatura.setProximoCupom(nova.cupom());
                assinatura.setProximaFormaPagamento(nova.forma());
            }
            case IMEDIATO -> {
                assinatura.setPlano(nova.plano());
                assinatura.setPeriodicidade(nova.periodicidade());
                assinatura.setCupom(nova.cupom());
                assinatura.setFormaPagamento(nova.forma());
                assinatura.setValor(nova.valor());
                assinatura.setDataInicioPeriodo(hoje);
                assinatura.setDataProximaCobranca(calculo.dataProximaCobranca());
                // o periodo novo e coberto pelo que foi cobrado agora ou pelo credito convertido
                assinatura.setValorPeriodo(calculo.valorCobradoAgora().signum() > 0
                        ? calculo.valorCobradoAgora() : calculo.credito());
                assinatura.limparTrocaAgendada();

                if (calculo.valorCobradoAgora().signum() > 0)
                    pagamentoRepository.persist(novoPagamento(assinatura, hoje));
            }
        }
    }

    // montado antes de aplicar a troca, enquanto a assinatura ainda tem o plano de antes
    private MudancaAssinatura registroDaTroca(Assinatura atual, Escolha nova, Calculo calculo, LocalDate hoje) {
        if (calculo.efeito() == EfeitoTroca.DESFAZ_AGENDAMENTO)
            return registroDoDesfazer(atual, hoje);

        TipoMudancaAssinatura tipo;
        if (calculo.efeito() == EfeitoTroca.IMEDIATO) {
            boolean aumenta = nova.periodicidade().valorMensal(nova.valor())
                    .compareTo(atual.getPeriodicidade().valorMensal(atual.getValor())) > 0;
            tipo = aumenta ? TipoMudancaAssinatura.UPGRADE : TipoMudancaAssinatura.TROCA_IMEDIATA;
        } else {
            tipo = nova.plano().isGratuito() ? TipoMudancaAssinatura.CANCELAMENTO_AGENDADO
                    : TipoMudancaAssinatura.DOWNGRADE_AGENDADO;
        }

        MudancaAssinatura mudanca = mudancaDoVigente(atual, tipo, OrigemMudancaAssinatura.ADMIN, hoje);
        mudanca.definirNovo(nova.plano(), nova.periodicidade(), nova.valor());
        if (calculo.efeito() == EfeitoTroca.AGENDADO)
            mudanca.setDetalhe("Vale a partir de " + calculo.dataProximaCobranca().format(DATA_BR) + ".");
        return mudanca;
    }

    // anterior = o que estava agendado; novo = o plano vigente, que continua
    private MudancaAssinatura registroDoDesfazer(Assinatura atual, LocalDate hoje) {
        MudancaAssinatura mudanca = novaMudanca(atual, TipoMudancaAssinatura.AGENDAMENTO_DESFEITO,
                OrigemMudancaAssinatura.ADMIN, hoje);
        mudanca.definirAnterior(atual.getProximoPlano(), atual.getProximaPeriodicidade(),
                atual.getProximoPlano().calcularValor(atual.getProximaPeriodicidade(), atual.getProximoCupom()));
        mudanca.definirNovo(atual.getPlano(), atual.getPeriodicidade(), atual.getValor());
        return mudanca;
    }

    private boolean isPlanoVigente(Assinatura atual, Escolha nova) {
        return atual.getPlano().getId().equals(nova.plano().getId())
                && atual.getPeriodicidade() == nova.periodicidade()
                && Objects.equals(idDo(atual.getCupom()), idDo(nova.cupom()));
    }

    private TrocaPlanoResponseDTO toResponse(Calculo calculo, Assinatura assinatura) {
        return new TrocaPlanoResponseDTO(
                calculo.efeito(),
                calculo.credito(),
                calculo.diasCobertos(),
                calculo.valorCobradoAgora(),
                calculo.dataProximaCobranca(),
                calculo.valorProximaCobranca(),
                AssinaturaResponseMapper.toResponse(assinatura));
    }

    // ===== usados tambem pela cobranca (CobrancaServiceImpl) =====

    // cobranca simulada (RN15): ja nasce aprovada, com o plano vigente copiado para o historico
    static Pagamento novoPagamento(Assinatura assinatura, LocalDate data) {
        Pagamento pagamento = new Pagamento();
        pagamento.setAssinatura(assinatura);
        pagamento.setPlano(assinatura.getPlano());
        pagamento.setPeriodicidade(assinatura.getPeriodicidade());
        pagamento.setCupom(assinatura.getCupom());
        pagamento.setValor(assinatura.getValor());
        if (assinatura.getCupom() != null) {
            BigDecimal cheio = assinatura.getPlano().calcularValor(assinatura.getPeriodicidade(), null);
            pagamento.setValorDesconto(cheio.subtract(assinatura.getValor()).max(ZERO));
        }
        pagamento.setFormaPagamento(assinatura.getFormaPagamento());
        pagamento.setStatus(StatusPagamento.APROVADO);
        pagamento.setDataPagamento(data);
        return pagamento;
    }

    static MudancaAssinatura novaMudanca(Assinatura assinatura, TipoMudancaAssinatura tipo,
            OrigemMudancaAssinatura origem, LocalDate data) {
        MudancaAssinatura mudanca = new MudancaAssinatura();
        mudanca.setAssinatura(assinatura);
        mudanca.setTipo(tipo);
        mudanca.setOrigem(origem);
        mudanca.setData(data);
        return mudanca;
    }

    // mudanca que parte do plano vigente (chamar antes de alterar a assinatura)
    static MudancaAssinatura mudancaDoVigente(Assinatura assinatura, TipoMudancaAssinatura tipo,
            OrigemMudancaAssinatura origem, LocalDate data) {
        MudancaAssinatura mudanca = novaMudanca(assinatura, tipo, origem, data);
        mudanca.definirAnterior(assinatura.getPlano(), assinatura.getPeriodicidade(), assinatura.getValor());
        return mudanca;
    }

    // plano Gratuito: sem cobranca, sem cupom e sem forma de pagamento (e sem recusa a simular)
    static void aplicarGratuito(Assinatura assinatura, Plano gratuito, LocalDate data) {
        assinatura.setPlano(gratuito);
        assinatura.setPeriodicidade(Periodicidade.MENSAL);
        assinatura.setCupom(null);
        assinatura.setFormaPagamento(FormaPagamento.NENHUMA);
        assinatura.setValor(ZERO);
        assinatura.setDataInicioPeriodo(data);
        assinatura.setDataProximaCobranca(null);
        assinatura.setValorPeriodo(ZERO);
        assinatura.limparTrocaAgendada();
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setTentativasRecusadas(0);
        assinatura.setSimularRecusa(false);
    }

    Plano buscarGratuitoOuFalhar() {
        Plano gratuito = planoRepository.findByNomeExato(Plano.NOME_GRATUITO);
        if (gratuito == null)
            throw ValidationException.of("idPlano",
                    "Plano \"" + Plano.NOME_GRATUITO + "\" não encontrado. Configure-o antes de continuar.");

        return gratuito;
    }

    // o alvo e a assinatura; o id do alvo e o do usuario (uma assinatura por usuario, RN16)
    private void auditar(AcaoAuditoria acao, Assinatura assinatura, Map<String, String> antes) {
        Usuario usuario = assinatura.getUsuario();
        auditoria.registrar(acao, TipoAlvoAuditoria.ASSINATURA, usuario.getId(), RetratoAuditoria.descricao(usuario),
                antes, RetratoAuditoria.de(assinatura));
    }

    // ===== validacoes =====

    private Assinatura buscarDeContaAtivaOuFalhar(long idUsuario) {
        Assinatura assinatura = buscarDoUsuario(idUsuario);
        if (!assinatura.getUsuario().getAtivo())
            throw ValidationException.of("idUsuario", "Conta desativada não pode trocar de plano.");
        return assinatura;
    }

    // o periodo esta parado na cobranca recusada: trocar agora misturaria credito com divida
    // (cancelar continua possivel: volta ao Gratuito na hora)
    private Assinatura buscarParaTrocaOuFalhar(long idUsuario) {
        Assinatura assinatura = buscarDeContaAtivaOuFalhar(idUsuario);
        if (assinatura.getStatus() == StatusAssinatura.PENDENTE)
            throw ValidationException.of("idPlano", "Há um pagamento recusado aguardando nova tentativa. "
                    + "A troca de plano volta a ser possível quando a cobrança for aprovada (ou cancele o plano pago).");

        return assinatura;
    }

    // plano e cupom vigentes continuam aceitos mesmo se forem desativados depois (para desfazer
    // um agendamento); os novos precisam estar ativos e o cupom dentro da validade
    private Escolha resolverEscolha(Assinatura atual, TrocaPlanoDTO dto) {
        Plano plano = atual.getPlano().getId().equals(dto.idPlano()) ? atual.getPlano()
                : buscarPlanoAtivoOuFalhar(dto.idPlano());

        Periodicidade periodicidade = Periodicidade.valueOf(dto.idPeriodicidade());
        if (periodicidade == null)
            throw ValidationException.of("idPeriodicidade", "Periodicidade não encontrada.");

        Cupom cupom = null;
        if (dto.idCupom() != null) {
            cupom = dto.idCupom().equals(idDo(atual.getCupom())) ? atual.getCupom()
                    : buscarCupomValidoOuFalhar(dto.idCupom());
            // o plano e o cupom vigentes juntos (desfazer um agendamento) continuam aceitos como estao
            if (!(cupom == atual.getCupom() && plano == atual.getPlano()))
                validarRegrasDoCupom(cupom, plano, atual);
        }

        BigDecimal valor = plano.calcularValor(periodicidade, cupom);

        return new Escolha(plano, periodicidade, cupom, resolverForma(atual, dto, valor), valor);
    }

    // sem custo nao ha pagamento; com custo vale a forma escolhida ou a que ja e usada nas cobrancas
    private FormaPagamento resolverForma(Assinatura atual, TrocaPlanoDTO dto, BigDecimal valor) {
        if (valor.signum() == 0)
            return FormaPagamento.NENHUMA;

        FormaPagamento forma = FormaPagamento.valueOf(dto.idFormaPagamento());
        if (dto.idFormaPagamento() != null && forma == null)
            throw ValidationException.of("idFormaPagamento", "Forma de pagamento não encontrada.");
        if (forma == null && atual.getFormaPagamento() != FormaPagamento.NENHUMA)
            forma = atual.getFormaPagamento();
        if (forma == null)
            throw ValidationException.of("idFormaPagamento", "Selecione a forma de pagamento.");
        if (forma == FormaPagamento.NENHUMA)
            throw ValidationException.of("idFormaPagamento",
                    "A forma de pagamento \"Nenhuma\" só vale para planos sem custo.");

        return forma;
    }

    private Plano buscarPlanoAtivoOuFalhar(Long idPlano) {
        Plano plano = planoRepository.findById(idPlano);
        if (plano == null)
            throw ValidationException.of("idPlano", "Plano não encontrado.");
        if (!plano.getAtivo())
            throw ValidationException.of("idPlano", "Este plano está inativo e não aceita novas assinaturas.");

        return plano;
    }

    // so cupom ativo e dentro da validade (o ultimo dia de validade ainda vale)
    private Cupom buscarCupomValidoOuFalhar(Long idCupom) {
        Cupom cupom = cupomRepository.findById(idCupom);
        if (cupom == null)
            throw ValidationException.of("idCupom", "Cupom não encontrado.");
        if (!cupom.getAtivo())
            throw ValidationException.of("idCupom", "Este cupom está inativo.");
        if (cupom.getValidade().isBefore(LocalDate.now()))
            throw ValidationException.of("idCupom", "Este cupom está expirado.");

        return cupom;
    }

    // restricao por plano e limite de usos (uso = adesao da assinatura; quem ja aderiu nao gasta outro)
    private void validarRegrasDoCupom(Cupom cupom, Plano plano, Assinatura atual) {
        boolean jaAderiu = cupom.getLimiteUsos() != null && cupomRepository.jaAderiu(cupom.getId(), atual.getId());
        String motivo = RegrasCupom.motivoParaNaoAplicar(cupom, plano, jaAderiu,
                () -> cupomRepository.countAdesoes(cupom.getId()));
        if (motivo != null)
            throw ValidationException.of("idCupom", motivo);
    }

    private static Long idDo(Cupom cupom) {
        return cupom == null ? null : cupom.getId();
    }
}
