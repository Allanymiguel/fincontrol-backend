package br.unitins.tp2.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "O e-mail deve ser informado.")
    String email,

    @NotBlank(message = "A senha deve ser informada.")
    String senha
) {
}
