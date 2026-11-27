package br.unitins.tp2.service;

import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    KeycloakTokenService keycloakTokenService;

    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public KeycloakTokenResponse login(LoginRequestDTO login) {
        return keycloakTokenService.login(login.email().trim().toLowerCase(), login.senha());
    }

    @Override
    public KeycloakTokenResponse renovar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new NaoAutenticadoException("Faça login para continuar.");

        return keycloakTokenService.renovar(refreshToken);
    }

    // sem sessao nao ha o que encerrar
    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            return;

        keycloakTokenService.logout(refreshToken);
    }

    // keycloakId = claim "sub" do token
    @Override
    public Usuario usuarioLogado(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank())
            throw new NaoAutenticadoException("Faça login para continuar.");

        Usuario usuario = usuarioRepository.findByKeycloakId(keycloakId);
        // usuario criado direto no console do Keycloak, sem o cadastro do FinControl
        if (usuario == null)
            throw new ResourceNotFoundException("Perfil do usuário não encontrado no FinControl.");

        return usuario;
    }
}
