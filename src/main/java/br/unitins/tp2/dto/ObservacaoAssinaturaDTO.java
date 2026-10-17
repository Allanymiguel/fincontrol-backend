package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

public record ObservacaoAssinaturaDTO(
    @Length(max = 255, message = "A observação deve conter no máximo 255 caracteres.")
    String observacao
) {
}
