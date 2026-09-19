package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransacaoRequestDTO(
    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres.")
    @NotBlank(message = "O campo descricao deve ser informado.")
    String descricao,

    @NotNull(message = "O campo valor deve ser informado.")
    @Positive(message = "O campo valor deve ser positivo.")
    BigDecimal valor,

    @NotNull(message = "O campo data deve ser informado.")
    LocalDate data,

    @NotNull(message = "O campo tipo deve ser informado.")
    Long idTipo,

    @NotNull(message = "O campo escopo deve ser informado.")
    Long idEscopo,

    @NotNull(message = "A categoria deve ser informada.")
    Long idCategoria
) {
}
