package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.service.UsuarioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Somente consulta e ativacao: os usuarios serao criados pelo Keycloak.
// TODO: @RolesAllowed("ADMIN") - gestao de usuarios e exclusiva do administrador (RN12)
@Path("admin/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioAdminResource {

    @Inject
    UsuarioService service;

    // busca = parte do nome ou do email
    @GET
    public PageResponse<UsuarioResponseDTO> buscarTodos(
            @QueryParam("busca") String busca,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        boolean hasBusca = busca != null && !busca.isBlank();

        List<Usuario> usuarios = hasBusca ? service.findByNomeOuEmail(busca.trim(), page, pageSize)
                : service.findAll(page, pageSize);
        long totalItems = hasBusca ? service.count(busca.trim()) : service.count();

        return PageResponse.of(usuarios, page, pageSize, totalItems, UsuarioResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathParam("id") Long id) {
        return UsuarioResponseMapper.toResponse(service.findById(id));
    }

    // devolve o usuario ja com o novo estado, para a listagem atualizar a linha
    @PATCH
    @Path("/{id}/ativar")
    public UsuarioResponseDTO alternarAtivo(@PathParam("id") Long id) {
        return UsuarioResponseMapper.toResponse(service.alternarAtivo(id));
    }
}
