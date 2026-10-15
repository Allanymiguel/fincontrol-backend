package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.ContatoRequestDTO;
import br.unitins.tp2.dto.ContatoResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.ContatoResponseMapper;
import br.unitins.tp2.model.Contato;
import br.unitins.tp2.service.ContatoService;
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

@Path("contatos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContatoResource {

    @Inject
    ContatoService service;

    @GET
    public PageResponse<ContatoResponseDTO> buscarTodos(
            @QueryParam("nome") String nome,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasNome = nome != null && !nome.isBlank();

        List<Contato> contatos = hasNome ? service.findByNome(nome, page, pageSize) : service.findAll(page, pageSize);
        long totalItems = hasNome ? service.count(nome) : service.count();

        return PageResponse.of(contatos, page, pageSize, totalItems, ContatoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public ContatoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return ContatoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public ContatoResponseDTO incluir(ContatoRequestDTO dto) {
        return ContatoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, ContatoRequestDTO contato) {
        service.update(id, contato);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
