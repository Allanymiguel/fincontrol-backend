package br.unitins.tp2.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.dto.CadastroCompletoDTO;
import br.unitins.tp2.dto.CadastroSimplesDTO;
import br.unitins.tp2.dto.EsqueciSenhaDTO;
import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.dto.TokenResponseDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.NaoAutenticadoExceptionMapper;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.security.CookieSessao;
import br.unitins.tp2.service.AuthService;
import br.unitins.tp2.service.UsuarioService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.ForbiddenException;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// Toda a autenticacao do front passa por aqui: o back conversa com o Keycloak. O access token vai no
// corpo da resposta e o refresh token num cookie HttpOnly.
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String CABECALHO_DO_APP = "X-Requested-With";

    @Inject
    AuthService service;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

    @Inject
    CookieSessao cookie;

    @Inject
    NaoAutenticadoExceptionMapper naoAutenticado;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequestDTO dto, @CookieParam(CookieSessao.NOME) String sessaoAnterior) {
        KeycloakTokenResponse tokens = service.login(dto);
        // entrar de novo no mesmo navegador nao deixa a sessao antiga aberta
        service.logout(sessaoAnterior);
        return comSessao(tokens);
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Consumes(MediaType.WILDCARD) // sem corpo: a sessao vem no cookie
    public Response renovar(@CookieParam(CookieSessao.NOME) String refreshToken,
            @HeaderParam(CABECALHO_DO_APP) String cabecalhoDoApp) {
        exigirChamadaDoApp(cabecalhoDoApp);
        try {
            return comSessao(service.renovar(refreshToken));
        } catch (NaoAutenticadoException e) {
            // sessao acabou: o cookie nao serve mais (com o Keycloak fora do ar ele e mantido)
            return Response.fromResponse(naoAutenticado.toResponse(e)).cookie(cookie.apagar()).build();
        }
    }

    @POST
    @Path("/logout")
    @PermitAll
    @Consumes(MediaType.WILDCARD) // sem corpo: a sessao vem no cookie
    public Response logout(@CookieParam(CookieSessao.NOME) String refreshToken,
            @HeaderParam(CABECALHO_DO_APP) String cabecalhoDoApp) {
        exigirChamadaDoApp(cabecalhoDoApp);
        service.logout(refreshToken);
        return Response.noContent().cookie(cookie.apagar()).build();
    }

    // o Keycloak envia o e-mail com o link; a resposta e a mesma exista a conta ou nao
    @POST
    @Path("/esqueci-senha")
    @PermitAll
    public void esqueciSenha(EsqueciSenhaDTO dto) {
        service.esqueciSenha(dto);
    }

    // Protecao contra CSRF nas chamadas que agem so com o cookie: um formulario de outro site nao
    // consegue enviar este cabecalho, e um script de outra origem e barrado pelo CORS
    private static void exigirChamadaDoApp(String cabecalho) {
        if (cabecalho == null || cabecalho.isBlank())
            throw new ForbiddenException("Cabeçalho " + CABECALHO_DO_APP + " ausente.");
    }

    private Response comSessao(KeycloakTokenResponse tokens) {
        return Response.ok(new TokenResponseDTO(tokens.accessToken(), tokens.expiresIn()))
                .cookie(cookie.criar(tokens.refreshToken(), tokens.refreshExpiresIn()))
                .header("Cache-Control", "no-store")
                .build();
    }

    // Cadastro publico, sempre com papel USER, em duas formas: simples (nome, e-mail e senha) ou
    // completo (com dados fiscais e endereco). Depois o usuario faz login normalmente.
    @POST
    @Path("/registro/simples")
    @PermitAll
    public UsuarioResponseDTO registrarSimples(CadastroSimplesDTO dto) {
        return UsuarioResponseMapper.toResponse(usuarioService.cadastrarSimples(dto, Papel.USER));
    }

    @POST
    @Path("/registro/completo")
    @PermitAll
    public UsuarioResponseDTO registrarCompleto(CadastroCompletoDTO dto) {
        return UsuarioResponseMapper.toResponse(usuarioService.cadastrarCompleto(dto, Papel.USER));
    }

    // perfil do usuario dono do token
    @GET
    @Path("/me")
    @Authenticated
    public UsuarioResponseDTO usuarioLogado() {
        return UsuarioResponseMapper.toResponse(service.usuarioLogado(jwt.getSubject()));
    }
}
