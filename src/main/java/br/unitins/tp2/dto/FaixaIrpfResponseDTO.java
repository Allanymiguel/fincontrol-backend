package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaixaIrpfResponseDTO(
    Long id,
    BigDecimal limiteInferior,
    BigDecimal limiteSuperior,
    BigDecimal aliquota,
    BigDecimal parcelaDeduzir,
    LocalDate vigenciaInicio
) {
}
