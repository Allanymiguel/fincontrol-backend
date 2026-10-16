package br.unitins.tp2.resource;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.dto.RefreshTokenRequestDTO;
import br.unitins.tp2.dto.RegistroRequestDTO;
import br.unitins.tp2.dto.TokenResponseDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
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

// O back nunca gera token: so repassa o que o Keycloak emitiu
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

    @POST
    @Path("/login")
    @PermitAll
    public TokenResponseDTO login(LoginRequestDTO dto) {
        return service.login(dto);
    }

    // cria a conta (Keycloak + perfil) com papel USER; depois o usuario faz login normalmente
    @POST
    @Path("/registro")
    @PermitAll
    public UsuarioResponseDTO registrar(RegistroRequestDTO dto) {
        return UsuarioResponseMapper.toResponse(usuarioService.registrar(dto));
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public TokenResponseDTO renovar(RefreshTokenRequestDTO dto) {
        return service.renovar(dto);
    }

    @POST
    @Path("/logout")
    @PermitAll
    public void logout(RefreshTokenRequestDTO dto) {
        service.logout(dto);
    }

    // perfil do usuario dono do token
    @GET
    @Path("/me")
    @Authenticated
    public UsuarioResponseDTO usuarioLogado() {
        return UsuarioResponseMapper.toResponse(service.usuarioLogado(jwt.getSubject()));
    }
}
