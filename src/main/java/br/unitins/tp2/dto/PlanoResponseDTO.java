package br.unitins.tp2.dto;

import java.math.BigDecimal;

public record PlanoResponseDTO(
    Long id,
    String nome,
    BigDecimal precoMensal,
    BigDecimal precoAnual,
    Integer limiteTransacoes,
    Integer limiteMetas,
    Boolean liberaRecorrencias,
    Boolean liberaFiscal,
    Boolean liberaExportacao,
    Boolean ativo
) {
}
