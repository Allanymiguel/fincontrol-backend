package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.dto.DashboardResponseDTO.ReceitaEmRisco;
import br.unitins.tp2.dto.DashboardResponseDTO.ValorQuantidade;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.model.Usuario;

// Indicadores do painel do admin, calculados sobre objetos em memoria (sem banco)
class IndicadoresAssinaturaTest {

    private final Plano gratuito = plano(Plano.NOME_GRATUITO, "0.00", "0.00");
    private final Plano essencial = plano("Essencial", "19.90", "199.00");
    private final Plano empreendedor = plano("Empreendedor", "39.90", "399.00");

    @Test
    void valorMensalDoAnualEDivididoPorDoze() {
        assertEquals(new BigDecimal("33.25"), Periodicidade.ANUAL.valorMensal(new BigDecimal("399.00")));
        assertEquals(new BigDecimal("39.90"), Periodicidade.MENSAL.valorMensal(new BigDecimal("39.90")));
        // 359,10 / 12 = 29,925 -> 29,93 (meio para cima)
        assertEquals(new BigDecimal("29.93"), Periodicidade.ANUAL.valorMensal(new BigDecimal("359.10")));
    }

    @Test
    void mrrSomaPlanosPagosDeContasAtivasEmValorMensal() {
        List<Assinatura> assinaturas = List.of(
                assinatura(empreendedor, Periodicidade.MENSAL, "39.90", true),
                // anual com cupom de 10%: o valor da assinatura ja vem com desconto
                assinatura(empreendedor, Periodicidade.ANUAL, "359.10", true),
                assinatura(essencial, Periodicidade.MENSAL, "19.90", true));

        ValorQuantidade mrr = IndicadoresAssinatura.calcularMrr(assinaturas);

        assertEquals(new BigDecimal("89.73"), mrr.valor());
        assertEquals(3, mrr.assinaturas());
    }

    @Test
    void mrrIgnoraGratuitoContaDesativadaEAssinaturaNaoAtiva() {
        Assinatura cancelada = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);
        cancelada.setStatus(StatusAssinatura.CANCELADA);
        List<Assinatura> assinaturas = List.of(
                assinatura(gratuito, Periodicidade.MENSAL, "0.00", true),
                assinatura(empreendedor, Periodicidade.MENSAL, "39.90", false),
                cancelada);

        ValorQuantidade mrr = IndicadoresAssinatura.calcularMrr(assinaturas);

        assertEquals(new BigDecimal("0.00"), mrr.valor());
        assertEquals(0, mrr.assinaturas());
    }

    @Test
    void receitaEmRiscoSomaDowngradeCancelamentoEContaDesativada() {
        Assinatura downgrade = assinatura(empreendedor, Periodicidade.MENSAL, "39.90", true);
        agendar(downgrade, essencial, Periodicidade.MENSAL, null);
        Assinatura cancelamento = assinatura(essencial, Periodicidade.ANUAL, "199.00", true);
        agendar(cancelamento, gratuito, Periodicidade.MENSAL, null);
        Assinatura desativada = assinatura(empreendedor, Periodicidade.MENSAL, "39.90", false);
        Assinatura semAgendamento = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);

        ReceitaEmRisco risco = IndicadoresAssinatura
                .calcularReceitaEmRisco(List.of(downgrade, cancelamento, desativada, semAgendamento));

        assertEquals(new BigDecimal("20.00"), risco.downgrades().valor());
        assertEquals(1, risco.downgrades().assinaturas());
        // 199,00 / 12 = 16,58
        assertEquals(new BigDecimal("16.58"), risco.cancelamentos().valor());
        assertEquals(new BigDecimal("39.90"), risco.contasDesativadas().valor());
        assertEquals(new BigDecimal("76.48"), risco.total());
    }

    @Test
    void downgradeAgendadoUsaOCupomAgendado() {
        Assinatura downgrade = assinatura(empreendedor, Periodicidade.MENSAL, "39.90", true);
        agendar(downgrade, essencial, Periodicidade.MENSAL, cupom("10.00"));

        ReceitaEmRisco risco = IndicadoresAssinatura.calcularReceitaEmRisco(List.of(downgrade));

        // Essencial com 10%: 17,91 -> perda de 39,90 - 17,91
        assertEquals(new BigDecimal("21.99"), risco.downgrades().valor());
    }

    @Test
    void contaDesativadaNaoEntraTambemComoDowngrade() {
        Assinatura desativada = assinatura(empreendedor, Periodicidade.MENSAL, "39.90", false);
        agendar(desativada, essencial, Periodicidade.MENSAL, null);

        ReceitaEmRisco risco = IndicadoresAssinatura.calcularReceitaEmRisco(List.of(desativada));

        assertEquals(new BigDecimal("0.00"), risco.downgrades().valor());
        assertEquals(new BigDecimal("39.90"), risco.contasDesativadas().valor());
        assertEquals(new BigDecimal("39.90"), risco.total());
    }

    @Test
    void pagamentoRecusadoSaiDoMrrEEntraNoRiscoAParte() {
        Assinatura pendente = assinatura(empreendedor, Periodicidade.MENSAL, "39.90", true);
        pendente.setStatus(StatusAssinatura.PENDENTE);
        // pendente e com conta desativada: a desativacao vence (volta ao Gratuito antes de tentar de novo)
        Assinatura pendenteDesativada = assinatura(essencial, Periodicidade.MENSAL, "19.90", false);
        pendenteDesativada.setStatus(StatusAssinatura.PENDENTE);

        ReceitaEmRisco risco = IndicadoresAssinatura.calcularReceitaEmRisco(List.of(pendente, pendenteDesativada));

        assertEquals(new BigDecimal("0.00"), IndicadoresAssinatura.calcularMrr(List.of(pendente)).valor());
        assertEquals(new BigDecimal("39.90"), risco.pagamentosRecusados().valor());
        assertEquals(1, risco.pagamentosRecusados().assinaturas());
        assertEquals(new BigDecimal("19.90"), risco.contasDesativadas().valor());
        assertEquals(new BigDecimal("59.80"), risco.total());
    }

    @Test
    void estornoContaNoMesEmQueFoiFeito() {
        Assinatura assinatura = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);
        Pagamento setembro = pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-09-10",
                StatusPagamento.ESTORNADO);
        setembro.setDataEstorno(LocalDate.of(2026, 10, 3).atTime(14, 0));
        Pagamento outubro = pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-10-10",
                StatusPagamento.APROVADO);
        List<Pagamento> pagamentos = List.of(setembro, outubro);

        // setembro recebeu (o estorno nao reescreve o mes do pagamento)...
        assertEquals(new BigDecimal("19.90"),
                IndicadoresAssinatura.receitaAprovadaDoMes(pagamentos, YearMonth.of(2026, 9)));
        assertEquals(new BigDecimal("0.00"), IndicadoresAssinatura.estornadoNoMes(pagamentos, YearMonth.of(2026, 9)));
        // ...e outubro devolveu
        assertEquals(new BigDecimal("19.90"),
                IndicadoresAssinatura.receitaAprovadaDoMes(pagamentos, YearMonth.of(2026, 10)));
        assertEquals(new BigDecimal("19.90"),
                IndicadoresAssinatura.estornadoNoMes(pagamentos, YearMonth.of(2026, 10)));
    }

    @Test
    void pagamentoEstornadoContinuaCobrindoOPeriodo() {
        Assinatura assinatura = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);
        Pagamento estornado = pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-10-10",
                StatusPagamento.ESTORNADO);
        estornado.setDataEstorno(LocalDate.of(2026, 10, 21).atTime(9, 40));

        assertEquals(Map.of(essencial, 1L),
                IndicadoresAssinatura.pagantesPorPlano(List.of(estornado), LocalDate.of(2026, 10, 31)));
    }

    @Test
    void receitaDoMesSomaSoAprovadosComDataNoMes() {
        Assinatura assinatura = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);
        List<Pagamento> pagamentos = List.of(
                pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-09-05", StatusPagamento.APROVADO),
                pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-09-20", StatusPagamento.RECUSADO),
                pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-10-05", StatusPagamento.APROVADO));

        assertEquals(new BigDecimal("19.90"),
                IndicadoresAssinatura.receitaAprovadaDoMes(pagamentos, YearMonth.of(2026, 9)));
        assertEquals(new BigDecimal("0.00"),
                IndicadoresAssinatura.receitaAprovadaDoMes(pagamentos, YearMonth.of(2026, 8)));
    }

    @Test
    void pagantePorPlanoEnquantoOPagamentoCobreAData() {
        Assinatura mensal = assinatura(essencial, Periodicidade.MENSAL, "19.90", true);
        Assinatura anual = assinatura(empreendedor, Periodicidade.ANUAL, "399.00", true);
        List<Pagamento> pagamentos = List.of(
                pagamento(mensal, essencial, Periodicidade.MENSAL, "19.90", "2026-09-05", StatusPagamento.APROVADO),
                pagamento(anual, empreendedor, Periodicidade.ANUAL, "399.00", "2026-02-01", StatusPagamento.APROVADO));

        Map<Plano, Long> setembro = IndicadoresAssinatura.pagantesPorPlano(pagamentos, LocalDate.of(2026, 9, 30));
        assertEquals(1L, setembro.get(essencial));
        assertEquals(1L, setembro.get(empreendedor));

        // o mensal pago em 05/09 cobre ate 04/10; o anual pago em 01/02 cobre ate 31/01 do ano seguinte
        Map<Plano, Long> outubro = IndicadoresAssinatura.pagantesPorPlano(pagamentos, LocalDate.of(2026, 10, 5));
        assertNull(outubro.get(essencial));
        assertEquals(1L, outubro.get(empreendedor));
    }

    @Test
    void pagantePorPlanoUsaOPagamentoMaisRecenteEIgnoraRecusado() {
        Assinatura assinatura = assinatura(empreendedor, Periodicidade.ANUAL, "399.00", true);
        List<Pagamento> pagamentos = List.of(
                pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-01-01", StatusPagamento.APROVADO),
                pagamento(assinatura, empreendedor, Periodicidade.ANUAL, "399.00", "2026-02-01", StatusPagamento.APROVADO),
                pagamento(assinatura, essencial, Periodicidade.MENSAL, "19.90", "2026-02-10", StatusPagamento.RECUSADO));

        Map<Plano, Long> pagantes = IndicadoresAssinatura.pagantesPorPlano(pagamentos, LocalDate.of(2026, 2, 28));

        assertEquals(Map.of(empreendedor, 1L), pagantes);
    }

    @Test
    void taxaDeConversaoSemCadastrosENulaEComCadastrosTemUmaCasa() {
        assertNull(IndicadoresAssinatura.taxaConversao(0, 0));
        assertEquals(new BigDecimal("37.5"), IndicadoresAssinatura.taxaConversao(8, 3));
        assertEquals(new BigDecimal("33.3"), IndicadoresAssinatura.taxaConversao(3, 1));
    }

    @Test
    void contasAteContaAsDatasAteODiaInclusive() {
        List<LocalDate> cadastros = List.of(LocalDate.of(2026, 5, 10), LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 2));

        assertEquals(3, IndicadoresAssinatura.contasAte(cadastros, LocalDate.of(2026, 6, 30)));
        assertEquals(0, IndicadoresAssinatura.contasAte(cadastros, LocalDate.of(2026, 5, 9)));
    }

    @Test
    void mesesDoPeriodoIncluemInicioEFim() {
        assertEquals(List.of(YearMonth.of(2026, 11), YearMonth.of(2026, 12), YearMonth.of(2027, 1)),
                IndicadoresAssinatura.meses(LocalDate.of(2026, 11, 20), LocalDate.of(2027, 1, 3)));
    }

    // ===== auxiliares =====

    private static Plano plano(String nome, String mensal, String anual) {
        Plano plano = new Plano();
        plano.setNome(nome);
        plano.setPrecoMensal(new BigDecimal(mensal));
        plano.setPrecoAnual(new BigDecimal(anual));
        plano.setAtivo(true);
        return plano;
    }

    private static Cupom cupom(String percentual) {
        Cupom cupom = new Cupom();
        cupom.setCodigo("TESTE");
        cupom.setPercentualDesconto(new BigDecimal(percentual));
        return cupom;
    }

    private static Assinatura assinatura(Plano plano, Periodicidade periodicidade, String valor, boolean contaAtiva) {
        Usuario usuario = new Usuario();
        usuario.setAtivo(contaAtiva);
        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setPlano(plano);
        assinatura.setPeriodicidade(periodicidade);
        assinatura.setValor(new BigDecimal(valor));
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setFormaPagamento(plano.isGratuito() ? FormaPagamento.NENHUMA : FormaPagamento.PIX);
        return assinatura;
    }

    private static void agendar(Assinatura assinatura, Plano plano, Periodicidade periodicidade, Cupom cupom) {
        assinatura.setProximoPlano(plano);
        assinatura.setProximaPeriodicidade(periodicidade);
        assinatura.setProximoCupom(cupom);
    }

    private static Pagamento pagamento(Assinatura assinatura, Plano plano, Periodicidade periodicidade, String valor,
            String data, StatusPagamento status) {
        Pagamento pagamento = new Pagamento();
        pagamento.setAssinatura(assinatura);
        pagamento.setPlano(plano);
        pagamento.setPeriodicidade(periodicidade);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setDataPagamento(LocalDate.parse(data));
        pagamento.setStatus(status);
        return pagamento;
    }
}
