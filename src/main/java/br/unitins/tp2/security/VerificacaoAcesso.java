package br.unitins.tp2.security;

import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.RegrasAcesso;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

// Leitura do perfil local para o ContaAtivaAugmentor (precisa de contexto de requisicao proprio,
// porque roda no meio da autenticacao)
@ApplicationScoped
public class VerificacaoAcesso {

    @Inject
    UsuarioRepository usuarioRepository;

    // nulo = pode seguir; sem perfil local, o fluxo de sempre trata (ex.: /auth/me responde 404)
    @ActivateRequestContext
    public String motivoParaRecusar(String keycloakId, String sid) {
        Usuario usuario = keycloakId == null ? null : usuarioRepository.findByKeycloakId(keycloakId);
        if (usuario == null)
            return null;
        return RegrasAcesso.motivoParaRecusar(Boolean.TRUE.equals(usuario.getAtivo()), usuario.getSessoesRevogadas(),
                sid);
    }
}
