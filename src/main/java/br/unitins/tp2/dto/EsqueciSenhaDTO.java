package br.unitins.tp2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaDTO(
    @NotBlank(message = "O e-mail deve ser informado.")
    @Email(message = "Informe um e-mail válido.")
    String email
) {
}
