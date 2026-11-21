package br.unitins.tp2.exception;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    UriInfo uri;

    @ConfigProperty(name = "problem.base-url")
    String baseUrl;

    @Override
    public Response toResponse(ResourceNotFoundException e) {
        var p = new Problem();
        p.type = baseUrl + "/errors/not-found";
        p.title = "Recurso não encontrado";
        p.status = Response.Status.NOT_FOUND.getStatusCode();
        p.detail = e.getMessage();
        p.instance = (uri != null ? uri.getRequestUri().getPath() : null);
        p.timestamp = OffsetDateTime.now();

        // erro nao vai para cache (ex.: a imagem do plano volta a existir quando o armazenamento voltar)
        return Response.status(p.status).type("application/problem+json").header("Cache-Control", "no-store")
                .entity(p).build();
    }
}
