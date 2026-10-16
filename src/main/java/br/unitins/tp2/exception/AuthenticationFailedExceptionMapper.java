package br.unitins.tp2.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

// Token invalido ou expirado (assinatura, emissor ou validade nao conferem)
@Provider
@ApplicationScoped
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(AuthenticationFailedException e) {
        var p = new Problem();
        p.type = baseUrl + "/errors/unauthorized";
        p.title = "Não autenticado";
        p.status = Response.Status.UNAUTHORIZED.getStatusCode();
        p.detail = "Sessão inválida ou expirada. Faça login novamente.";
        p.instance = (uri != null ? uri.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();

        return Response.status(p.status).type("application/problem+json").entity(p).build();
    }
}
