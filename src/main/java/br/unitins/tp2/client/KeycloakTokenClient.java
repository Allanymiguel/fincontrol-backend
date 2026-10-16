package br.unitins.tp2.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// Endpoints OpenID Connect do realm. A URL base vem do application.properties
// (quarkus.rest-client.keycloak-token.url = .../realms/{realm}/protocol/openid-connect)
@RegisterRestClient(configKey = "keycloak-token")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface KeycloakTokenClient {

    // login com e-mail e senha (password grant)
    @POST
    @Path("/token")
    KeycloakTokenResponse senha(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("username") String username,
            @FormParam("password") String password);

    // troca o refresh token por um novo access token
    @POST
    @Path("/token")
    KeycloakTokenResponse renovar(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken);

    // token do proprio client (service account), usado nas chamadas da Admin API
    @POST
    @Path("/token")
    KeycloakTokenResponse clientCredentials(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret);

    // encerra a sessao do usuario no Keycloak (invalida o refresh token)
    @POST
    @Path("/logout")
    void logout(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken);
}
