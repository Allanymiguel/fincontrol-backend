package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParametroFiscalResponseDTO(
    Long id,
    String chave,
    BigDecimal valor,
    LocalDate vigenciaInicio,
    LocalDate vigenciaFim,
    // calculado: o parametro vale hoje
    Boolean vigente
) {
}
