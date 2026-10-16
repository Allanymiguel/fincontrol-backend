package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CupomResponseDTO(
    Long id,
    String codigo,
    BigDecimal percentualDesconto,
    LocalDate validade,
    Boolean ativo
) {
}
