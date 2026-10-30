package br.unitins.tp2.security;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Roda em toda requisicao autenticada, depois de o token ser validado: barra na hora a conta
// desativada e o token de uma sessao que o admin encerrou. Sem isso, o access token (30 min no
// realm) continuaria valendo ate expirar.
@ApplicationScoped
public class ContaAtivaAugmentor implements SecurityIdentityAugmentor {

    @Inject
    VerificacaoAcesso verificacao;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous() || !(identity.getPrincipal() instanceof JsonWebToken token))
            return Uni.createFrom().item(identity);
        String keycloakId = token.getSubject();
        String sid = token.getClaim("sid");
        // consulta ao banco: fora da thread de I/O
        return context.runBlocking(() -> {
            String motivo = verificacao.motivoParaRecusar(keycloakId, sid);
            if (motivo != null)
                throw new AuthenticationFailedException(motivo);
            return identity;
        });
    }
}
