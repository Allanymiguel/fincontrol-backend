package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record FaixaIrpfRequestDTO(
    @NotNull(message = "O limite inferior deve ser informado.")
    @PositiveOrZero(message = "O limite inferior não pode ser negativo.")
    @Digits(integer = 12, fraction = 2, message = "O limite inferior deve ter no máximo 2 casas decimais.")
    BigDecimal limiteInferior,

    // opcional: nulo na ultima faixa
    @PositiveOrZero(message = "O limite superior não pode ser negativo.")
    @Digits(integer = 12, fraction = 2, message = "O limite superior deve ter no máximo 2 casas decimais.")
    BigDecimal limiteSuperior,

    @NotNull(message = "A alíquota deve ser informada.")
    @PositiveOrZero(message = "A alíquota não pode ser negativa.")
    @DecimalMax(value = "100", message = "A alíquota deve ser no máximo 100.")
    @Digits(integer = 3, fraction = 2, message = "A alíquota deve ter no máximo 2 casas decimais.")
    BigDecimal aliquota,

    @NotNull(message = "A parcela a deduzir deve ser informada.")
    @PositiveOrZero(message = "A parcela a deduzir não pode ser negativa.")
    @Digits(integer = 12, fraction = 2, message = "A parcela a deduzir deve ter no máximo 2 casas decimais.")
    BigDecimal parcelaDeduzir,

    @NotNull(message = "O início da vigência deve ser informado.")
    LocalDate vigenciaInicio
) {
}
