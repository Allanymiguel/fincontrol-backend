package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Indicadores do painel do admin. So a relacao do usuario com o FinControl (assinatura, plano,
// pagamento): nada das financas pessoais. As formulas estao no Swagger (DashboardResource).
public record DashboardResponseDTO(
    LocalDate inicio,
    LocalDate fim,
    // hoje: assinaturas pagas de contas ativas, em valor mensal
    ValorQuantidade mrr,
    ReceitaEmRisco receitaEmRisco,
    // mes da data final do periodo
    ReceitaMensal receitaMesReferencia,
    List<ReceitaMensal> receitaPorMes,
    // hoje: contas ativas por plano vigente
    List<PlanoQuantidade> assinantesPorPlano,
    List<EvolucaoMensal> evolucao,
    Cadastros cadastros
) {

    public record ValorQuantidade(BigDecimal valor, long assinaturas) {
    }

    public record ReceitaEmRisco(
        BigDecimal total,
        // perda de MRR dos downgrades agendados
        ValorQuantidade downgrades,
        // MRR das assinaturas com volta ao Gratuito agendada
        ValorQuantidade cancelamentos,
        // MRR de contas desativadas com plano pago (fora do MRR: voltam ao Gratuito na proxima cobranca)
        ValorQuantidade contasDesativadas,
        // valor mensal das assinaturas com pagamento recusado em nova tentativa (fora do MRR: voltam ao
        // Gratuito na 3a recusa seguida)
        ValorQuantidade pagamentosRecusados
    ) {
    }

    // mes = "AAAA-MM". aprovado: pagamentos com data no mes (inclusive os estornados depois); estornado:
    // estornos feitos no mes; liquido = aprovado - estornado. semHistorico = mes anterior ao primeiro
    // dado do sistema: valores nulos
    public record ReceitaMensal(String mes, boolean semHistorico, BigDecimal aprovado, BigDecimal estornado,
            BigDecimal liquido) {
    }

    public record PlanoQuantidade(Long idPlano, String nomePlano, boolean gratuito, long quantidade) {
    }

    // reconstruida dos pagamentos: pagantes por plano no fim do mes e contas cadastradas ate la
    public record EvolucaoMensal(String mes, boolean semHistorico, Long contasCadastradas,
            List<PlanoQuantidade> pagantesPorPlano) {
    }

    // taxaConversao: percentual dos novos que ja pagaram algum plano; nula sem cadastros no periodo
    public record Cadastros(long novos, long convertidos, BigDecimal taxaConversao) {
    }
}
