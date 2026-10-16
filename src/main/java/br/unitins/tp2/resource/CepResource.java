package br.unitins.tp2.resource;

import br.unitins.tp2.dto.CepResponseDTO;
import br.unitins.tp2.service.CepService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// O front consulta o CEP por aqui (e nao direto no ViaCEP) para receber o municipio
// ja resolvido no nosso banco, com o id pronto para salvar no cadastro.
@RolesAllowed({ "USER", "ADMIN" })
@Path("cep")
@Produces(MediaType.APPLICATION_JSON)
public class CepResource {

    @Inject
    CepService service;

    @GET
    @Path("/{cep}")
    public CepResponseDTO buscar(@PathParam("cep") String cep) {
        return service.buscar(cep);
    }
}
