package br.unitins.tp2.dto;

// Tokens emitidos pelo Keycloak, repassados ao front. Tempos em segundos.
public record TokenResponseDTO(
    String accessToken,
    Long expiresIn,
    String refreshToken,
    Long refreshExpiresIn
) {
}
