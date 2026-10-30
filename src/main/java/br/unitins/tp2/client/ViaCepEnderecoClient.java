package br.unitins.tp2.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// ViaCEP so para o endereco de CLIENTES (base propria de enderecos), com timeout curto
// (quarkus.rest-client.viacep-endereco.*). O ViaCepClient dos contatos continua como esta.
@RegisterRestClient(configKey = "viacep-endereco")
@Path("/ws")
public interface ViaCepEnderecoClient {

    @GET
    @Path("/{cep}/json")
    @Produces(MediaType.APPLICATION_JSON)
    ViaCepResponse buscar(@PathParam("cep") String cep);
}
