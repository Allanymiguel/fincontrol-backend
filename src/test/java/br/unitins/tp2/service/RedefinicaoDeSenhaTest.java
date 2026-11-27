package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.unitins.tp2.client.KeycloakAdminClient;
import br.unitins.tp2.client.KeycloakUsuario;
import br.unitins.tp2.exception.ServicoExternoException;

// "Esqueci minha senha" com a Admin API do Keycloak simulada
class RedefinicaoDeSenhaTest {

    private KeycloakAdminClient client;
    private KeycloakAdminService service;

    @BeforeEach
    void preparar() {
        client = mock(KeycloakAdminClient.class);
        KeycloakTokenService tokens = mock(KeycloakTokenService.class);
        when(tokens.tokenDeAdmin()).thenReturn("token");
        service = new KeycloakAdminService();
        service.adminClient = client;
        service.keycloakTokenService = tokens;
        service.clientId = "fincontrol-backend";
        service.urlDoFront = "http://localhost:4200";
    }

    private void contaEncontrada(boolean ativa) {
        when(client.buscarPorEmail("Bearer token", "a@b.com", true)).thenReturn(List.of(
                new KeycloakUsuario("id-1", "a@b.com", "a@b.com", "Ana", "Lima", ativa, true, null)));
    }

    private void nenhumEmailEnviado() {
        verify(client, never()).enviarEmailDeAcoes(anyString(), anyString(), anyString(), anyString(), anyInt(), any());
    }

    @Test
    void contaAtivaRecebeOLinkDeNovaSenha() {
        contaEncontrada(true);

        service.enviarRedefinicaoDeSenha("a@b.com");

        verify(client).enviarEmailDeAcoes("Bearer token", "id-1", "fincontrol-backend",
                "http://localhost:4200/login", 1800, List.of("UPDATE_PASSWORD"));
    }

    @Test
    void contaDesativadaNaoRecebeNada() {
        contaEncontrada(false);

        service.enviarRedefinicaoDeSenha("a@b.com");

        nenhumEmailEnviado();
    }

    @Test
    void emailSemContaNaoGeraErro() {
        assertDoesNotThrow(() -> service.enviarRedefinicaoDeSenha("ninguem@b.com"));

        nenhumEmailEnviado();
    }

    @Test
    void falhaNoEnvioNaoRevelaQueAContaExiste() {
        contaEncontrada(true);
        doThrow(new RuntimeException("SMTP")).when(client).enviarEmailDeAcoes(anyString(), anyString(), anyString(),
                anyString(), anyInt(), any());

        assertDoesNotThrow(() -> service.enviarRedefinicaoDeSenha("a@b.com"));
    }

    @Test
    void keycloakForaDoArEServicoIndisponivel() {
        when(client.buscarPorEmail("Bearer token", "a@b.com", true))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(ServicoExternoException.class, () -> service.enviarRedefinicaoDeSenha("a@b.com"));
    }
}
