package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CategoriaPadraoRequestDTO;
import br.unitins.tp2.dto.CategoriaPadraoResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.CategoriaPadraoResponseMapper;
import br.unitins.tp2.model.CategoriaPadrao;
import br.unitins.tp2.service.CategoriaPadraoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Sem DELETE: categoria padrao e arquivada, nunca excluida.
// TODO: @RolesAllowed("ADMIN") - cadastro de categorias padrao e exclusivo do administrador (RN12)
@Path("categorias-padrao")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaPadraoResource {

    @Inject
    CategoriaPadraoService service;

    @GET
    public PageResponse<CategoriaPadraoResponseDTO> buscarTodos(
            @QueryParam("nome") String nome,
            @QueryParam("idTipo") Long idTipo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<CategoriaPadrao> categorias = service.findByFiltros(nome, idTipo, page, pageSize);
        long totalItems = service.count(nome, idTipo);

        return PageResponse.of(categorias, page, pageSize, totalItems, CategoriaPadraoResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public CategoriaPadraoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return CategoriaPadraoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public CategoriaPadraoResponseDTO incluir(CategoriaPadraoRequestDTO dto) {
        return CategoriaPadraoResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CategoriaPadraoRequestDTO categoria) {
        service.update(id, categoria);
    }

    // devolve a categoria ja com o novo estado, para a listagem atualizar a linha
    @PATCH
    @Path("/{id}/arquivar")
    public CategoriaPadraoResponseDTO alternarArquivada(@PathParam("id") Long id) {
        return CategoriaPadraoResponseMapper.toResponse(service.alternarArquivada(id));
    }
}
