package br.unitins.tp2.service;

import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

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
