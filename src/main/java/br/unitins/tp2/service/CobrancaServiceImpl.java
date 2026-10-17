package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jboss.logging.Logger;

import br.unitins.tp2.dto.CobrancaResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// Cobranca das assinaturas (RN16). Para cada assinatura com a proxima cobranca vencida:
// 1. conta desativada nao e cobrada: volta ao Gratuito;
// 2. a troca agendada (plano mais barato ou cancelamento), se houver, passa a valer;
// 3. plano sem custo: deixa de ter cobranca. Plano pago: registra o Pagamento (simulado e ja
//    aprovado, RN15) e o periodo avanca um ciclo. Se varios ciclos venceram, cobra um por um.
@ApplicationScoped
public class CobrancaServiceImpl implements CobrancaService {

    private static final Logger LOG = Logger.getLogger(CobrancaServiceImpl.class);

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    PlanoRepository planoRepository;

    @Scheduled(cron = "{fincontrol.cobranca.cron}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void cobrarDiariamente() {
        CobrancaResponseDTO resultado = processar(LocalDate.now());
        LOG.infof("Cobranca de %s: %d assinatura(s), %d pagamento(s), R$ %s", resultado.data(),
                resultado.assinaturasProcessadas(), resultado.pagamentosGerados(), resultado.valorTotal());
    }

    @Override
    @Transactional
    public CobrancaResponseDTO processar(LocalDate data) {
        int assinaturas = 0, pagamentos = 0, trocas = 0, gratuitos = 0;
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        Plano gratuito = null;

        for (Assinatura assinatura : assinaturaRepository.findComCobrancaAte(data)) {
            assinaturas++;

            while (assinatura.getDataProximaCobranca() != null && !assinatura.getDataProximaCobranca().isAfter(data)) {
                LocalDate dia = assinatura.getDataProximaCobranca();

                if (!assinatura.getUsuario().getAtivo()) {
                    gratuito = gratuito != null ? gratuito : buscarGratuitoOuFalhar();
                    AssinaturaServiceImpl.aplicarGratuito(assinatura, gratuito, dia);
                    gratuitos++;
                    break;
                }

                if (assinatura.isTrocaAgendada()) {
                    aplicarTrocaAgendada(assinatura);
                    trocas++;
                }

                // o preco e recalculado na cobranca: um reajuste do plano vale a partir do proximo ciclo
                BigDecimal valor = assinatura.getPlano().calcularValor(assinatura.getPeriodicidade(),
                        assinatura.getCupom());
                assinatura.setValor(valor);

                if (valor.signum() == 0) {
                    gratuitos += Plano.NOME_GRATUITO.equals(assinatura.getPlano().getNome()) ? 1 : 0;
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
        }

        return new CobrancaResponseDTO(data, assinaturas, pagamentos, total, trocas, gratuitos);
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
}
