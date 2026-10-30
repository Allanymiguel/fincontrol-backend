package br.unitins.tp2.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.DashboardResponseDTO.ReceitaEmRisco;
import br.unitins.tp2.dto.DashboardResponseDTO.ValorQuantidade;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.StatusPagamento;

// Calculos do painel do admin sobre dados ja carregados (sem banco, para testar isolado).
// Usa so a relacao do usuario com o FinControl (Assinatura, Plano, Pagamento), nunca as financas pessoais.
public final class IndicadoresAssinatura {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal CEM = new BigDecimal("100");

    private IndicadoresAssinatura() {
    }

    private static boolean temPlanoPago(Assinatura assinatura) {
        return !assinatura.getPlano().isGratuito() && assinatura.getValor().signum() > 0;
    }

    // plano pago valendo: ATIVA, fora do Gratuito e com valor de cobranca
    private static boolean isPaga(Assinatura assinatura) {
        return assinatura.getStatus() == StatusAssinatura.ATIVA && temPlanoPago(assinatura);
    }

    // pagamento recusado aguardando nova tentativa (volta ao Gratuito na 3a recusa)
    private static boolean isPendente(Assinatura assinatura) {
        return assinatura.getStatus() == StatusAssinatura.PENDENTE && temPlanoPago(assinatura);
    }

    // conta como pago: aprovado, mesmo que estornado depois (o estorno so mexe na receita, nao na assinatura)
    private static boolean foiPago(Pagamento pagamento) {
        return pagamento.getStatus() == StatusPagamento.APROVADO || pagamento.getStatus() == StatusPagamento.ESTORNADO;
    }

    private static boolean isContaAtiva(Assinatura assinatura) {
        return Boolean.TRUE.equals(assinatura.getUsuario().getAtivo());
    }

    // entra no MRR: plano pago de conta ativa (a desativada volta ao Gratuito na proxima cobranca)
    public static boolean contaNoMrr(Assinatura assinatura) {
        return isPaga(assinatura) && isContaAtiva(assinatura);
    }

    // valor de cada cobranca (ja com o cupom) como equivalente mensal
    public static BigDecimal valorMensal(Assinatura assinatura) {
        return assinatura.getPeriodicidade().valorMensal(assinatura.getValor());
    }

    // equivalente mensal do que foi agendado para a proxima cobranca, com o cupom agendado
    public static BigDecimal valorMensalAgendado(Assinatura assinatura) {
        BigDecimal valor = assinatura.getProximoPlano().calcularValor(assinatura.getProximaPeriodicidade(),
                assinatura.getProximoCupom());
        return assinatura.getProximaPeriodicidade().valorMensal(valor);
    }

    public static ValorQuantidade calcularMrr(Collection<Assinatura> assinaturas) {
        BigDecimal total = ZERO;
        long quantidade = 0;
        for (Assinatura assinatura : assinaturas) {
            if (!contaNoMrr(assinatura))
                continue;
            total = total.add(valorMensal(assinatura));
            quantidade++;
        }
        return new ValorQuantidade(total, quantidade);
    }

    // perda de MRR ja anunciada: downgrades agendados (a diferenca), cancelamentos agendados (o MRR
    // inteiro), contas desativadas com plano pago e assinaturas com pagamento recusado (essas duas
    // fora do MRR, com o valor mensal inteiro, mostradas a parte)
    public static ReceitaEmRisco calcularReceitaEmRisco(Collection<Assinatura> assinaturas) {
        BigDecimal downgrades = ZERO, cancelamentos = ZERO, desativadas = ZERO, recusados = ZERO;
        long qtdDowngrades = 0, qtdCancelamentos = 0, qtdDesativadas = 0, qtdRecusados = 0;

        for (Assinatura assinatura : assinaturas) {
            if (!isPaga(assinatura) && !isPendente(assinatura))
                continue;
            // a conta desativada volta ao Gratuito na proxima cobranca, antes de qualquer nova tentativa
            if (!isContaAtiva(assinatura)) {
                desativadas = desativadas.add(valorMensal(assinatura));
                qtdDesativadas++;
                continue;
            }
            if (isPendente(assinatura)) {
                recusados = recusados.add(valorMensal(assinatura));
                qtdRecusados++;
                continue;
            }
            if (!assinatura.isTrocaAgendada())
                continue;
            if (assinatura.getProximoPlano().isGratuito()) {
                cancelamentos = cancelamentos.add(valorMensal(assinatura));
                qtdCancelamentos++;
                continue;
            }
            BigDecimal perda = valorMensal(assinatura).subtract(valorMensalAgendado(assinatura));
            if (perda.signum() > 0) {
                downgrades = downgrades.add(perda);
                qtdDowngrades++;
            }
        }

        return new ReceitaEmRisco(
                downgrades.add(cancelamentos).add(desativadas).add(recusados),
                new ValorQuantidade(downgrades, qtdDowngrades),
                new ValorQuantidade(cancelamentos, qtdCancelamentos),
                new ValorQuantidade(desativadas, qtdDesativadas),
                new ValorQuantidade(recusados, qtdRecusados));
    }

    // pagamentos aprovados com data no mes, inclusive os estornados depois (o estorno sai no mes dele)
    public static BigDecimal receitaAprovadaDoMes(Collection<Pagamento> pagamentos, YearMonth mes) {
        BigDecimal total = ZERO;
        for (Pagamento pagamento : pagamentos) {
            if (foiPago(pagamento) && YearMonth.from(pagamento.getDataPagamento()).equals(mes))
                total = total.add(pagamento.getValor());
        }
        return total;
    }

    // estornos feitos no mes, de pagamentos de qualquer mes
    public static BigDecimal estornadoNoMes(Collection<Pagamento> pagamentos, YearMonth mes) {
        BigDecimal total = ZERO;
        for (Pagamento pagamento : pagamentos) {
            if (pagamento.getStatus() == StatusPagamento.ESTORNADO && pagamento.getDataEstorno() != null
                    && YearMonth.from(pagamento.getDataEstorno()).equals(mes))
                total = total.add(pagamento.getValor());
        }
        return total;
    }

    // Reconstrucao pelos pagamentos: para cada assinatura vale o pagamento aprovado mais recente ate
    // a data (estornado tambem: o estorno nao mexe na assinatura); se ele ainda cobre a data (um ciclo
    // a partir do pagamento), a assinatura conta como pagante do plano daquele pagamento. Recusado
    // nao cobre nada. Planos na ordem em que aparecem.
    public static Map<Plano, Long> pagantesPorPlano(Collection<Pagamento> pagamentos, LocalDate data) {
        Map<Assinatura, Pagamento> ultimoPorAssinatura = new IdentityHashMap<>();
        for (Pagamento pagamento : pagamentos) {
            if (!foiPago(pagamento) || pagamento.getDataPagamento().isAfter(data))
                continue;
            Pagamento atual = ultimoPorAssinatura.get(pagamento.getAssinatura());
            if (atual == null || pagamento.getDataPagamento().isAfter(atual.getDataPagamento()))
                ultimoPorAssinatura.put(pagamento.getAssinatura(), pagamento);
        }

        Map<Plano, Long> pagantes = new LinkedHashMap<>();
        for (Pagamento pagamento : ultimoPorAssinatura.values()) {
            LocalDate fimCobertura = pagamento.getPeriodicidade().somarCiclo(pagamento.getDataPagamento());
            if (data.isBefore(fimCobertura))
                pagantes.merge(pagamento.getPlano(), 1L, Long::sum);
        }
        return pagantes;
    }

    // quantas datas de cadastro sao ate a data (inclusive)
    public static long contasAte(Collection<LocalDate> datasCadastro, LocalDate data) {
        return datasCadastro.stream().filter(cadastro -> !cadastro.isAfter(data)).count();
    }

    // percentual com uma casa; nulo quando nao houve cadastro (nao e "0%")
    public static BigDecimal taxaConversao(long novos, long convertidos) {
        if (novos == 0)
            return null;
        return BigDecimal.valueOf(convertidos).multiply(CEM).divide(BigDecimal.valueOf(novos), 1,
                RoundingMode.HALF_UP);
    }

    // meses de inicio a fim, inclusive
    public static List<YearMonth> meses(LocalDate inicio, LocalDate fim) {
        List<YearMonth> meses = new ArrayList<>();
        for (YearMonth mes = YearMonth.from(inicio); !mes.isAfter(YearMonth.from(fim)); mes = mes.plusMonths(1))
            meses.add(mes);
        return meses;
    }
}
