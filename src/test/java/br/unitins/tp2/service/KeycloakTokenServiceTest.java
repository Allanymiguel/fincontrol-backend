package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.tp2.client.KeycloakErro;
import br.unitins.tp2.client.KeycloakTokenClient;
import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ServicoExternoException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

// Login, renovacao e logout com o endpoint de token do Keycloak simulado
class KeycloakTokenServiceTest {

    private static final KeycloakTokenResponse TOKENS = new KeycloakTokenResponse("access", 1800L, "refresh", 28800L);

    private KeycloakTokenClient client;
    private KeycloakTokenService service;

    @BeforeEach
    void preparar() {
        client = mock(KeycloakTokenClient.class);
        service = new KeycloakTokenService();
        service.tokenClient = client;
        service.clientId = "fincontrol-backend";
        service.clientSecret = "segredo";
    }

    // o WebApplicationException le status e corpo da Response
    private static WebApplicationException erroDoKeycloak(int status, String erro, String descricao) {
        Response resposta = mock(Response.class);
        when(resposta.getStatus()).thenReturn(status);
        when(resposta.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(status));
        when(resposta.readEntity(KeycloakErro.class)).thenReturn(new KeycloakErro(erro, descricao, null));
        return new WebApplicationException(resposta);
    }

    private void loginResponde(RuntimeException erro) {
        when(client.senha("password", "fincontrol-backend", "segredo", "a@b.com", "senha", "openid")).thenThrow(erro);
    }

    private String mensagemDoLogin() {
        return assertThrows(NaoAutenticadoException.class, () -> service.login("a@b.com", "senha")).getMessage();
    }

    @Test
    void loginDevolveOsTokensDoKeycloak() {
        when(client.senha("password", "fincontrol-backend", "segredo", "a@b.com", "senha", "openid"))
                .thenReturn(TOKENS);

        assertSame(TOKENS, service.login("a@b.com", "senha"));
    }

    @Test
    void senhaErradaViraNaoAutenticado() {
        loginResponde(erroDoKeycloak(401, "invalid_grant", "Invalid user credentials"));

        assertEquals("E-mail ou senha inválidos.", mensagemDoLogin());
    }

    @Test
    void contaDesativadaTemMensagemPropria() {
        loginResponde(erroDoKeycloak(400, "invalid_grant", "Account disabled"));

        assertEquals("Conta desativada. Fale com o administrador.", mensagemDoLogin());
    }

    @Test
    void secretErradoEServicoIndisponivel() {
        loginResponde(erroDoKeycloak(401, "invalid_client", "Invalid client credentials"));

        assertThrows(ServicoExternoException.class, () -> service.login("a@b.com", "senha"));
    }

    @Test
    void keycloakForaDoArEServicoIndisponivel() {
        loginResponde(new RuntimeException("Connection refused"));

        assertThrows(ServicoExternoException.class, () -> service.login("a@b.com", "senha"));
    }

    @Test
    void renovarComSessaoEncerradaViraNaoAutenticado() {
        WebApplicationException erro = erroDoKeycloak(400, "invalid_grant", "Session not active");
        when(client.renovar("refresh_token", "fincontrol-backend", "segredo", "velho")).thenThrow(erro);

        assertThrows(NaoAutenticadoException.class, () -> service.renovar("velho"));
    }

    @Test
    void renovarComKeycloakForaDoArNaoEncerraASessao() {
        when(client.renovar("refresh_token", "fincontrol-backend", "segredo", "refresh"))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(ServicoExternoException.class, () -> service.renovar("refresh"));
    }

    @Test
    void logoutEncerraASessaoNoKeycloak() {
        assertTrue(service.logout("refresh"));

        verify(client).logout("fincontrol-backend", "segredo", "refresh");
    }

    @Test
    void logoutQueFalhaNaoLancaErro() {
        doThrow(new RuntimeException("Connection refused")).when(client).logout("fincontrol-backend", "segredo",
                "refresh");

        assertFalse(service.logout("refresh"));
    }
}
