package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.TransacaoRequestDTO;
import br.unitins.tp2.dto.TransacaoResponseDTO;
import br.unitins.tp2.mapper.TransacaoResponseMapper;
import br.unitins.tp2.model.Transacao;
import br.unitins.tp2.service.TransacaoService;
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

@Path("transacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransacaoResource {

    @Inject
    TransacaoService service;

    @GET
    public PageResponse<TransacaoResponseDTO> buscarTodos(
            @QueryParam("descricao") String descricao,
            @QueryParam("idCategoria") Long idCategoria,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasDescricao = descricao != null && !descricao.isBlank();
        boolean hasCategoria = idCategoria != null;

        List<Transacao> transacoes;
        long totalItems;

        if (hasDescricao && hasCategoria) {
            transacoes = service.findByDescricaoAndCategoria(descricao, idCategoria, page, pageSize);
            totalItems = service.count(descricao, idCategoria);
        } else if (hasDescricao) {
            transacoes = service.findByDescricao(descricao, page, pageSize);
            totalItems = service.count(descricao);
        } else if (hasCategoria) {
            transacoes = service.findByCategoria(idCategoria, page, pageSize);
            totalItems = service.countByCategoria(idCategoria);
        } else {
            transacoes = service.findAll(page, pageSize);
            totalItems = service.count();
        }

        return PageResponse.of(transacoes, page, pageSize, totalItems, TransacaoResponseMapper::toResponse);
    }

    @GET
    @Path("/descricao/{descricao}")
    public PageResponse<TransacaoResponseDTO> buscarPorDescricao(@PathParam("descricao") String descricao,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Transacao> transacoes = service.findByDescricao(descricao, page, pageSize);
        long totalItems = service.count(descricao);

        return PageResponse.of(transacoes, page, pageSize, totalItems, TransacaoResponseMapper::toResponse);
    }

    @GET
    @Path("/categoria/{idCategoria}")
    public PageResponse<TransacaoResponseDTO> buscarPorCategoria(@PathParam("idCategoria") Long idCategoria,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        List<Transacao> transacoes = service.findByCategoria(idCategoria, page, pageSize);
        long totalItems = service.countByCategoria(idCategoria);

        return PageResponse.of(transacoes, page, pageSize, totalItems, TransacaoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public TransacaoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return TransacaoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public TransacaoResponseDTO incluir(TransacaoRequestDTO dto) {
        return TransacaoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, TransacaoRequestDTO transacao) {
        service.update(id, transacao);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }
}
