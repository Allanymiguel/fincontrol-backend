package br.unitins.tp2.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

// UserRepresentation do Keycloak. Campos nulos nao sao enviados,
// entao o mesmo record serve para criar e para atualizacao parcial.
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUsuario(
    String id,
    String username,
    String email,
    String firstName,
    String lastName,
    Boolean enabled,
    Boolean emailVerified,
    List<Credencial> credentials
) {

    public static KeycloakUsuario soAtivo(boolean ativo) {
        return new KeycloakUsuario(null, null, null, null, null, ativo, null, null);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Credencial(String type, String value, Boolean temporary) {

        public static Credencial senha(String senha) {
            return new Credencial("password", senha, false);
        }
    }
}
