package br.unitins.tp2.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
    @NotBlank(message = "O refresh token deve ser informado.")
    String refreshToken
) {
}
