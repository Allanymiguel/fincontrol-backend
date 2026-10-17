package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.ContatoCompletoDTO;
import br.unitins.tp2.dto.ContatoSimplesDTO;
import br.unitins.tp2.dto.ContatoResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.ContatoResponseMapper;
import br.unitins.tp2.model.Contato;
import br.unitins.tp2.service.ContatoService;
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

// TODO (RN12): devolver so os contatos do usuario logado (falta o vinculo com Usuario)
@RolesAllowed("USER")
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

    // cadastro simples: nome, tipo e telefone (e-mail opcional)
    @POST
    @Path("/simples")
    public ContatoResponseDTO incluirSimples(ContatoSimplesDTO dto) {
        return ContatoResponseMapper.toResponse(service.createSimples(dto));
    }

    // cadastro completo: tambem CPF/CNPJ e endereco
    @POST
    @Path("/completo")
    public ContatoResponseDTO incluirCompleto(ContatoCompletoDTO dto) {
        return ContatoResponseMapper.toResponse(service.createCompleto(dto));
    }

    // so para contato simples; um completo nao volta a ser simples
    @PUT
    @Path("/{id}/simples")
    public void alterarSimples(@PathParam("id") Long id, ContatoSimplesDTO contato) {
        service.updateSimples(id, contato);
    }

    // completa um contato simples ou atualiza um completo
    @PUT
    @Path("/{id}/completo")
    public void alterarCompleto(@PathParam("id") Long id, ContatoCompletoDTO contato) {
        service.updateCompleto(id, contato);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
