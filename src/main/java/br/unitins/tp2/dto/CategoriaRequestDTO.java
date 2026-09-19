package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequestDTO(
    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres.")
    @NotBlank(message = "O campo nome deve ser informado.")
    String nome,

    @NotNull(message = "O campo tipo deve ser informado.")
    Long idTipo,

    @Length(max = 7, message = "O campo cor deve ter no máximo 7 caracteres.")
    String cor,

    @NotNull(message = "O campo ativa deve ser informado.")
    Boolean ativa
) {
}
