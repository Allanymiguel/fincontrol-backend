package br.unitins.tp2.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

// Logado, mas sem a role exigida (ex.: USER acessando a area ADMIN)
@Provider
@ApplicationScoped
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ForbiddenException e) {
        var p = new Problem();
        p.type = baseUrl + "/errors/forbidden";
        p.title = "Acesso negado";
        p.status = Response.Status.FORBIDDEN.getStatusCode();
        p.detail = "Você não tem permissão para acessar este recurso.";
        p.instance = (uri != null ? uri.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();

        return Response.status(p.status).type("application/problem+json").entity(p).build();
    }
}
