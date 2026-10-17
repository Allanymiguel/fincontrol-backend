package br.unitins.tp2.resource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Somente consulta e ativacao: as contas sao criadas pelo cadastro (POST /auth/registro).
// Gestao de usuarios e exclusiva do administrador (RN12)
@RolesAllowed("ADMIN")
@Path("admin/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioAdminResource {

    @Inject
    UsuarioService service;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    JsonWebToken jwt;

    // busca = parte do nome ou do email; idPlano = plano vigente da assinatura. Cada linha traz o
    // resumo da assinatura (as da pagina vem numa consulta so).
    @GET
    public PageResponse<UsuarioResponseDTO> buscarTodos(
            @QueryParam("busca") String busca,
            @QueryParam("idPlano") Long idPlano,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<Usuario> usuarios = service.findByFiltros(busca, idPlano, page, pageSize);
        long totalItems = service.count(busca, idPlano);

        Map<Long, Assinatura> assinaturas = assinaturaRepository
                .findByUsuarios(usuarios.stream().map(Usuario::getId).toList()).stream()
                .collect(Collectors.toMap(a -> a.getUsuario().getId(), Function.identity()));

        return PageResponse.of(usuarios, page, pageSize, totalItems,
                u -> UsuarioResponseMapper.toResponse(u, assinaturas.get(u.getId())));
    }

    @GET
    @Path("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathParam("id") Long id) {
        return comAssinatura(service.findById(id));
    }

    // resumo nao invasivo: perfil, assinaturas e contagens (sem o conteudo dos registros pessoais)
    @GET
    @Path("/{id}/resumo")
    public UsuarioResumoResponseDTO buscarResumo(@PathParam("id") Long id) {
        return service.buscarResumo(id);
    }

    // devolve o usuario ja com o novo estado, para a listagem atualizar a linha.
    // O sub do token impede o admin de desativar a propria conta.
    @PATCH
    @Path("/{id}/ativar")
    public UsuarioResponseDTO alternarAtivo(@PathParam("id") Long id) {
        return comAssinatura(service.alternarAtivo(id, jwt.getSubject()));
    }

    private UsuarioResponseDTO comAssinatura(Usuario usuario) {
        return UsuarioResponseMapper.toResponse(usuario, assinaturaRepository.findByUsuario(usuario.getId()));
    }
}
