package br.unitins.tp2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PlanoCatalogoResponseDTO;
import br.unitins.tp2.mapper.PlanoResponseMapper;
import br.unitins.tp2.service.PlanoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Catalogo de planos para o usuario (com o proprio plano destacado) e para o admin conferir as imagens.
// Recurso separado do PlanoResource, que e todo de ADMIN.
@RolesAllowed({ "USER", "ADMIN" })
@Path("planos/catalogo")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Planos")
public class CatalogoPlanoResource {

    @Inject
    PlanoService service;

    @GET
    @Operation(summary = "Catálogo de planos", description = "Planos ativos ordenados pelo preço mensal, mais o "
            + "plano atual do usuário mesmo se inativo (atual = true). Para o admin, atual é sempre false.")
    public PageResponse<PlanoCatalogoResponseDTO> listar(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("12") int pageSize) {
        PlanoService.Catalogo catalogo = service.catalogo(page, pageSize);
        return PageResponse.of(catalogo.planos(), page, pageSize, catalogo.total(),
                plano -> PlanoResponseMapper.toCatalogo(plano, catalogo.idPlanoAtual()));
    }
}
