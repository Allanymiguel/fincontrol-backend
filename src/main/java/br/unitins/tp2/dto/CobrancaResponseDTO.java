package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Resumo de uma rodada de cobranca
public record CobrancaResponseDTO(
    LocalDate data,
    // assinaturas com cobranca vencida ate a data
    int assinaturasProcessadas,
    int pagamentosGerados,
    BigDecimal valorTotal,
    // trocas agendadas que passaram a valer (inclui cancelamentos)
    int trocasAplicadas,
    // assinaturas que voltaram ao Gratuito (cancelamento ou conta desativada)
    int voltasAoGratuito
) {
}
