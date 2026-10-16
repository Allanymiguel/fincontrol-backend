package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// "arquivada" nao vem aqui: so muda pelo PATCH /categorias-padrao/{id}/arquivar
public record CategoriaPadraoRequestDTO(
    @Length(min = 2, max = 60, message = "O nome deve conter entre 2 e 60 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @NotNull(message = "O tipo deve ser informado.")
    Long idTipo,

    @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "A cor deve estar no formato hexadecimal #RRGGBB.")
    String cor
) {
}
