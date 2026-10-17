package br.unitins.tp2.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Resolve o Usuario local a partir do claim "sub" do token (keycloakId).
// Usado pelos services operacionais para escopar os dados ao usuario logado (RN12).
@ApplicationScoped
public class UsuarioLogadoService {

    @Inject
    JsonWebToken jwt;

    @Inject
    UsuarioRepository usuarioRepository;

    // Fallback do modo dev (FINCONTROL_AUTH=false): sem token, usa esse e-mail como usuario simulado.
    // Em producao com Keycloak ligado o fallback nunca e consultado.
    @ConfigProperty(name = "fincontrol.auth.dev-fallback-email", defaultValue = "joao@email.com")
    String devFallbackEmail;

    public Usuario atual() {
        String keycloakId = jwt.getSubject();

        if (keycloakId == null || keycloakId.isBlank()) {
            // sem token: modo dev sem autenticacao
            Usuario fallback = usuarioRepository.findByEmail(devFallbackEmail);
            if (fallback == null)
                throw new NaoAutenticadoException("Faça login para continuar.");

            return fallback;
        }

        Usuario usuario = usuarioRepository.findByKeycloakId(keycloakId);
        if (usuario == null)
            throw new ResourceNotFoundException("Perfil do usuário não encontrado no FinControl.");

        return usuario;
    }

    public long idAtual() {
        return atual().getId();
    }
}
