package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.AssinaturaRequestDTO;
import br.unitins.tp2.dto.AssinaturaResponseDTO;
import br.unitins.tp2.dto.AssinaturaUpdateDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.mapper.AssinaturaResponseMapper;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.service.AssinaturaService;
import jakarta.annotation.security.RolesAllowed;
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

// Sem DELETE: assinatura e cancelada (PATCH /cancelar), para manter o historico do usuario.
// Gestao de assinaturas e exclusiva do administrador (RN12)
@RolesAllowed("ADMIN")
@Path("admin/assinaturas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssinaturaResource {

    @Inject
    AssinaturaService service;

    // busca = parte do nome do usuario ou do plano; idStatus = filtro opcional
    @GET
    public PageResponse<AssinaturaResponseDTO> buscarTodos(
            @QueryParam("busca") String busca,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<Assinatura> assinaturas = service.findByFiltros(busca, idStatus, page, pageSize);
        long totalItems = service.count(busca, idStatus);

        return PageResponse.of(assinaturas, page, pageSize, totalItems, AssinaturaResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public AssinaturaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return AssinaturaResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public AssinaturaResponseDTO incluir(AssinaturaRequestDTO dto) {
        return AssinaturaResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, AssinaturaUpdateDTO assinatura) {
        service.update(id, assinatura);
    }

    // devolve a assinatura ja cancelada, para a listagem atualizar a linha
    @PATCH
    @Path("/{id}/cancelar")
    public AssinaturaResponseDTO cancelar(@PathParam("id") Long id) {
        return AssinaturaResponseMapper.toResponse(service.cancelar(id));
    }
}
