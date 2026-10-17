package br.unitins.tp2.resource;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.dto.CadastroCompletoDTO;
import br.unitins.tp2.dto.CadastroSimplesDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.dto.UsuarioEdicaoCompletaDTO;
import br.unitins.tp2.dto.UsuarioEdicaoSimplesDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.service.UsuarioService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Gestao de usuarios, exclusiva do administrador (RN12): consulta, criacao (cadastro simples ou
// completo, com papel USER ou ADMIN), edicao e ativacao. O usuario tambem pode se cadastrar
// sozinho (POST /auth/registro/...), sempre como USER.
@RolesAllowed("ADMIN")
@Path("admin/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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

    // cadastro simples: so nome, e-mail, senha e papel
    @POST
    @Path("/simples")
    public UsuarioResponseDTO cadastrarSimples(CadastroSimplesDTO dto) {
        return comAssinatura(service.cadastrarSimples(dto, papelInformado(dto == null ? null : dto.idPapel())));
    }

    // cadastro completo: tambem dados fiscais e endereco
    @POST
    @Path("/completo")
    public UsuarioResponseDTO cadastrarCompleto(CadastroCompletoDTO dto) {
        return comAssinatura(service.cadastrarCompleto(dto, papelInformado(dto == null ? null : dto.idPapel())));
    }

    @GET
    @Path("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathParam("id") Long id) {
        return comAssinatura(service.findById(id));
    }

    // edicao de um cadastro simples: so nome e e-mail (recusada se o cadastro ja for completo)
    @PUT
    @Path("/{id}/simples")
    public UsuarioResponseDTO editarSimples(@PathParam("id") Long id, UsuarioEdicaoSimplesDTO dto) {
        return comAssinatura(service.editarSimples(id, dto));
    }

    // completa um cadastro simples ou atualiza um completo; papel e senha nao mudam aqui
    @PUT
    @Path("/{id}/completo")
    public UsuarioResponseDTO editarCompleto(@PathParam("id") Long id, UsuarioEdicaoCompletaDTO dto) {
        return comAssinatura(service.editarCompleto(id, dto));
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

    // no cadastro pelo admin o papel e obrigatorio (1 = USER, 2 = ADMIN)
    private Papel papelInformado(Long idPapel) {
        Papel papel = Papel.valueOf(idPapel);
        if (papel == null)
            throw ValidationException.of("idPapel", "Selecione o papel do usuário.");
        return papel;
    }

    private UsuarioResponseDTO comAssinatura(Usuario usuario) {
        return UsuarioResponseMapper.toResponse(usuario, assinaturaRepository.findByUsuario(usuario.getId()));
    }
}
