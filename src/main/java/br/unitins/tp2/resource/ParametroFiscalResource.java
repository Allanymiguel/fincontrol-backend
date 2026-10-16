package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.ParametroFiscalRequestDTO;
import br.unitins.tp2.dto.ParametroFiscalResponseDTO;
import br.unitins.tp2.mapper.ParametroFiscalResponseMapper;
import br.unitins.tp2.model.ParametroFiscal;
import br.unitins.tp2.service.ParametroFiscalService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// TODO: @RolesAllowed("ADMIN") - cadastro de parametros fiscais e exclusivo do administrador (RN12)
@Path("parametros-fiscais")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ParametroFiscalResource {

    @Inject
    ParametroFiscalService service;

    @GET
    public PageResponse<ParametroFiscalResponseDTO> buscarTodos(
            @QueryParam("chave") String chave,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasChave = chave != null && !chave.isBlank();

        List<ParametroFiscal> parametros = hasChave ? service.findByChave(chave, page, pageSize)
                : service.findAll(page, pageSize);
        long totalItems = hasChave ? service.count(chave) : service.count();

        return PageResponse.of(parametros, page, pageSize, totalItems, ParametroFiscalResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public ParametroFiscalResponseDTO buscarPorId(@PathParam("id") Long id) {
        return ParametroFiscalResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public ParametroFiscalResponseDTO incluir(ParametroFiscalRequestDTO dto) {
        return ParametroFiscalResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, ParametroFiscalRequestDTO parametro) {
        service.update(id, parametro);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
