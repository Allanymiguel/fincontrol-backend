package br.unitins.tp2.service;

import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.unitins.tp2.client.KeycloakErro;
import br.unitins.tp2.client.KeycloakTokenClient;
import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.exception.ServicoExternoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

// Unico ponto que fala com o endpoint de token do Keycloak: o token da service account, usado pela
// Admin API. O login dos usuarios nao passa pelo back (e feito direto no Keycloak, pelo front).
@ApplicationScoped
public class KeycloakTokenService {

    private static final Logger LOG = Logger.getLogger(KeycloakTokenService.class);

    // renova o token da service account um pouco antes de expirar
    private static final long MARGEM_EXPIRACAO_SEGUNDOS = 30;

    @Inject
    @RestClient
    KeycloakTokenClient tokenClient;

    @ConfigProperty(name = "fincontrol.keycloak.client-id")
    String clientId;

    @ConfigProperty(name = "fincontrol.keycloak.client-secret")
    String clientSecret;

    private String tokenAdmin;
    private Instant tokenAdminExpiraEm = Instant.EPOCH;

    // token da service account do client, com cache ate perto de expirar
    public synchronized String tokenDeAdmin() {
        if (tokenAdmin != null && Instant.now().isBefore(tokenAdminExpiraEm))
            return tokenAdmin;

        try {
            KeycloakTokenResponse resposta = tokenClient.clientCredentials("client_credentials", clientId, clientSecret);
            tokenAdmin = resposta.accessToken();
            tokenAdminExpiraEm = Instant.now().plusSeconds(resposta.expiresIn() - MARGEM_EXPIRACAO_SEGUNDOS);
            return tokenAdmin;
        } catch (WebApplicationException e) {
            throw erroDeConfiguracao(e);
        } catch (RuntimeException e) {
            throw indisponivel(e);
        }
    }

    // forca buscar outro token na proxima chamada (ex.: a Admin API respondeu 401)
    public synchronized void descartarTokenDeAdmin() {
        tokenAdmin = null;
    }

    public static KeycloakErro lerErro(WebApplicationException e) {
        try {
            return e.getResponse().readEntity(KeycloakErro.class);
        } catch (RuntimeException ignorada) {
            return new KeycloakErro(null, null, null);
        }
    }

    private ServicoExternoException erroDeConfiguracao(WebApplicationException e) {
        KeycloakErro erro = lerErro(e);
        LOG.errorf("Keycloak recusou a requisição (HTTP %d, %s: %s). Confira client-id/secret do realm.",
                e.getResponse().getStatus(), erro.error(), erro.errorDescription());
        return new ServicoExternoException("O serviço de login está indisponível no momento.", e);
    }

    private ServicoExternoException indisponivel(RuntimeException e) {
        LOG.warnf(e, "Não foi possível conectar ao Keycloak");
        return new ServicoExternoException("O serviço de login está indisponível no momento.", e);
    }
}
