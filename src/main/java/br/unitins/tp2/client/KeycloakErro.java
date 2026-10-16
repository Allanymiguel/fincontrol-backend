package br.unitins.tp2.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Corpo de erro do Keycloak, ex.: {"error":"invalid_grant","error_description":"Account disabled"}
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakErro(
    String error,
    @JsonProperty("error_description") String errorDescription,
    // a Admin API usa "errorMessage" (ex.: 409 "User exists with same email")
    String errorMessage
) {
}
