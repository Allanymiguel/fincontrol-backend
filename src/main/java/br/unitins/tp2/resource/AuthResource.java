package br.unitins.tp2.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.dto.CadastroCompletoDTO;
import br.unitins.tp2.dto.CadastroSimplesDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.service.AuthService;
import br.unitins.tp2.service.UsuarioService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// O login, a renovacao e o logout sao feitos direto no Keycloak (Authorization Code com PKCE, pelo
// front). Aqui ficam so o cadastro publico e o perfil do dono do token.
@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService service;

    @Inject
    UsuarioService usuarioService;

    @Inject
    JsonWebToken jwt;

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
