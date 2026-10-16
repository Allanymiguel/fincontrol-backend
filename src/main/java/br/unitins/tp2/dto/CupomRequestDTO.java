package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CupomRequestDTO(
    @NotBlank(message = "O código deve ser informado.")
    @Pattern(regexp = "\\s*[A-Za-z0-9_-]{3,30}\\s*", message = "O código deve ter de 3 a 30 caracteres: letras, números, '-' ou '_'.")
    String codigo,

    @NotNull(message = "O percentual de desconto deve ser informado.")
    @DecimalMin(value = "0", inclusive = false, message = "O percentual de desconto deve ser maior que zero.")
    @DecimalMax(value = "100", message = "O percentual de desconto deve ser no máximo 100.")
    @Digits(integer = 3, fraction = 2, message = "O percentual de desconto deve ter no máximo 2 casas decimais.")
    BigDecimal percentualDesconto,

    @NotNull(message = "A validade deve ser informada.")
    LocalDate validade,

    @NotNull(message = "O campo ativo deve ser informado.")
    Boolean ativo
) {
}
