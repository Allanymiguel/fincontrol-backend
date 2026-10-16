package br.unitins.tp2.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// RoleRepresentation do Keycloak (so o necessario para atribuir a role a um usuario)
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakRole(String id, String name) {
}
