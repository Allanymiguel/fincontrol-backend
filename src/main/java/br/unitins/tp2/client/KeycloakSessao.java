package br.unitins.tp2.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Sessao ativa de um usuario no Keycloak (GET /users/{id}/sessions): o id e o claim "sid" do token
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakSessao(String id) {
}
