package br.unitins.tp2.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Admin REST API do realm. A URL base vem do application.properties
// (quarkus.rest-client.keycloak-admin.url = .../admin/realms/{realm}).
// "autorizacao" = "Bearer <token da service account do client>"
@RegisterRestClient(configKey = "keycloak-admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface KeycloakAdminClient {

    // 201 com o id do usuario novo no header Location; 409 se o e-mail ja existe
    @POST
    @Path("/users")
    Response criarUsuario(@HeaderParam("Authorization") String autorizacao, KeycloakUsuario usuario);

    // atualizacao parcial: so os campos enviados mudam (ex.: {"enabled": false})
    @PUT
    @Path("/users/{id}")
    void atualizarUsuario(@HeaderParam("Authorization") String autorizacao, @PathParam("id") String id,
            KeycloakUsuario usuario);

    @DELETE
    @Path("/users/{id}")
    void excluirUsuario(@HeaderParam("Authorization") String autorizacao, @PathParam("id") String id);

    @GET
    @Path("/roles/{nome}")
    KeycloakRole buscarRole(@HeaderParam("Authorization") String autorizacao, @PathParam("nome") String nome);

    @POST
    @Path("/users/{id}/role-mappings/realm")
    void atribuirRoles(@HeaderParam("Authorization") String autorizacao, @PathParam("id") String id,
            List<KeycloakRole> roles);
}
