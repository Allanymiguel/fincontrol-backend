package br.unitins.tp2.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// A URL base vem do application.properties (quarkus.rest-client.viacep.url)
@RegisterRestClient(configKey = "viacep")
@Path("/ws")
public interface ViaCepClient {

    @GET
    @Path("/{cep}/json")
    @Produces(MediaType.APPLICATION_JSON)
    ViaCepResponse buscar(@PathParam("cep") String cep);
}
