package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.dto.PlanoResponseDTO;
import br.unitins.tp2.mapper.PlanoResponseMapper;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.service.PlanoService;
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

// TODO: @RolesAllowed("ADMIN") - cadastro de planos e exclusivo do administrador (RN12)
@Path("planos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlanoResource {

    @Inject
    PlanoService service;

    @GET
    public PageResponse<PlanoResponseDTO> buscarTodos(
            @QueryParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasNome = nome != null && !nome.isBlank();

        List<Plano> planos = hasNome ? service.findByNome(nome, page, pageSize) : service.findAll(page, pageSize);
        long totalItems = hasNome ? service.count(nome) : service.count();

        return PageResponse.of(planos, page, pageSize, totalItems, PlanoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public PlanoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PlanoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public PlanoResponseDTO incluir(PlanoRequestDTO dto) {
        return PlanoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, PlanoRequestDTO plano) {
        service.update(id, plano);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
