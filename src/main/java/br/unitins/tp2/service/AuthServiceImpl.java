package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.dto.EsqueciSenhaDTO;
import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.RegrasAcesso;
import br.unitins.tp2.util.SessaoDoToken;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    KeycloakTokenService keycloakTokenService;

    @Inject
    KeycloakAdminService keycloakAdminService;

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

    @Override
    public void esqueciSenha(EsqueciSenhaDTO dto) {
        keycloakAdminService.enviarRedefinicaoDeSenha(dto.email().trim().toLowerCase());
    }

    // sem sessao nao ha o que encerrar. O access token ja emitido continuaria valendo ate expirar,
    // por isso a sessao entra na lista que o ContaAtivaAugmentor recusa
    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            return;

        // so confia no conteudo do token se o Keycloak o aceitou
        if (keycloakTokenService.logout(refreshToken))
            revogar(SessaoDoToken.ler(refreshToken));
    }

    private void revogar(SessaoDoToken sessao) {
        if (sessao == null)
            return;

        QuarkusTransaction.requiringNew().run(() -> {
            Usuario usuario = usuarioRepository.findByKeycloakId(sessao.keycloakId());
            if (usuario != null)
                usuario.setSessoesRevogadas(
                        RegrasAcesso.acrescentar(usuario.getSessoesRevogadas(), List.of(sessao.sid())));
        });
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
