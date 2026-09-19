package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CategoriaRequestDTO;
import br.unitins.tp2.dto.CategoriaResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.CategoriaResponseMapper;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.service.CategoriaService;
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

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    CategoriaService service;

    @GET
    public PageResponse<CategoriaResponseDTO> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Categoria> categorias = service.findAll(page, pageSize);
        long totalItems = service.count();

        return PageResponse.of(categorias, page, pageSize, totalItems, CategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/nome/{nome}")
    public PageResponse<CategoriaResponseDTO> buscarPorNome(@PathParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Categoria> categorias = service.findByNome(nome, page, pageSize);
        long totalItems = service.count(nome);

        return PageResponse.of(categorias, page, pageSize, totalItems, CategoriaResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public CategoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return CategoriaResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public CategoriaResponseDTO incluir(CategoriaRequestDTO dto) {
        return CategoriaResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CategoriaRequestDTO categoria) {
        service.update(id, categoria);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
