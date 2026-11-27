package br.unitins.tp2.util;

import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Dono ("sub") e sessao ("sid") de um token do Keycloak. NAO confere a assinatura: use somente com
// um token que o proprio Keycloak acabou de aceitar.
public record SessaoDoToken(String keycloakId, String sid) {

    private static final ObjectMapper JSON = new ObjectMapper();

    // nulo = token ilegivel ou sem as duas informacoes
    public static SessaoDoToken ler(String token) {
        try {
            JsonNode claims = JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
            String sub = claims.path("sub").asText("");
            String sid = claims.path("sid").asText("");
            return sub.isEmpty() || sid.isEmpty() ? null : new SessaoDoToken(sub, sid);
        } catch (Exception e) {
            return null;
        }
    }
}
