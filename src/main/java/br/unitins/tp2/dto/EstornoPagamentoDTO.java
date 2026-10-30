package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;

public record EstornoPagamentoDTO(
    @NotBlank(message = "Informe o motivo do estorno.")
    @Length(min = 5, max = 255, message = "O motivo deve ter entre 5 e 255 caracteres.")
    String motivo
) {
}
