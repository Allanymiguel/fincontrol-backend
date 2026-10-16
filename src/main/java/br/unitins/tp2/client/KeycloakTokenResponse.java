package br.unitins.tp2.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Resposta do endpoint /token do Keycloak (tempos em segundos)
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") Long expiresIn,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("refresh_expires_in") Long refreshExpiresIn,
    @JsonProperty("token_type") String tokenType
) {
}
