package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CupomRequestDTO;
import br.unitins.tp2.dto.CupomResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.CupomResponseMapper;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.service.CupomService;
import jakarta.annotation.security.RolesAllowed;
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

// Cadastro de cupons e exclusivo do administrador (RN12)
@RolesAllowed("ADMIN")
@Path("cupons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CupomResource {

    @Inject
    CupomService service;

    @GET
    public PageResponse<CupomResponseDTO> buscarTodos(
            @QueryParam("codigo") String codigo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasCodigo = codigo != null && !codigo.isBlank();

        List<Cupom> cupons = hasCodigo ? service.findByCodigo(codigo, page, pageSize) : service.findAll(page, pageSize);
        long totalItems = hasCodigo ? service.count(codigo) : service.count();

        return PageResponse.of(cupons, page, pageSize, totalItems, CupomResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public CupomResponseDTO buscarPorId(@PathParam("id") Long id) {
        return CupomResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public CupomResponseDTO incluir(CupomRequestDTO dto) {
        return CupomResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CupomRequestDTO cupom) {
        service.update(id, cupom);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
