package br.unitins.tp2.dto;

// Access token emitido pelo Keycloak (tempo em segundos). O refresh token nao vai no corpo:
// segue num cookie HttpOnly, fora do alcance do JavaScript.
public record TokenResponseDTO(
    String accessToken,
    Long expiresIn
) {
}
