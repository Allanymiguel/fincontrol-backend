package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record ParametroFiscalRequestDTO(
    @NotBlank(message = "A chave deve ser informada.")
    @Pattern(regexp = "\\s*[A-Za-z0-9_]{2,60}\\s*", message = "A chave deve ter de 2 a 60 caracteres: letras, números ou '_' (ex.: SALARIO_MINIMO).")
    String chave,

    @NotNull(message = "O valor deve ser informado.")
    @PositiveOrZero(message = "O valor não pode ser negativo.")
    @Digits(integer = 10, fraction = 4, message = "O valor deve ter no máximo 10 dígitos inteiros e 4 casas decimais.")
    BigDecimal valor,

    @NotNull(message = "O início da vigência deve ser informado.")
    LocalDate vigenciaInicio,

    // opcional: nulo = sem data de fim
    LocalDate vigenciaFim
) {
}
