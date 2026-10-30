package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.StatusExecucaoCobranca;

// Resumo de uma rodada de cobranca (tambem gravado como ExecucaoCobranca)
public record CobrancaResponseDTO(
    LocalDate data,
    // assinaturas com cobranca vencida ate a data
    int assinaturasProcessadas,
    int pagamentosGerados,
    BigDecimal valorTotal,
    // trocas agendadas que passaram a valer (inclui cancelamentos)
    int trocasAplicadas,
    // assinaturas que voltaram ao Gratuito (cancelamento ou conta desativada)
    int voltasAoGratuito,
    // tentativas recusadas (simulacao de recusa ligada no detalhe do usuario)
    int pagamentosRecusados,
    // execucao registrada no historico e como ela terminou
    Long idExecucao,
    StatusExecucaoCobranca status
) {
}
