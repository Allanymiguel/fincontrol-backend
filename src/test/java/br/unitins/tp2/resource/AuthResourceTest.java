package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.exception.NaoAutenticadoException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.security.VerificacaoAcesso;
import br.unitins.tp2.service.KeycloakTokenService;
import br.unitins.tp2.testes.Tokens;
import br.unitins.tp2.testes.Usuarios;
import br.unitins.tp2.util.RegrasAcesso;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;

// Login, renovacao e logout pela API, com o Keycloak simulado
@QuarkusTest
class AuthResourceTest {

    private static final String COOKIE = "fincontrol_refresh";
    private static final KeycloakTokenResponse TOKENS = new KeycloakTokenResponse("access-1", 1800L, "refresh-1",
            28800L);

    @InjectMock
    KeycloakTokenService keycloak;

    @Inject
    VerificacaoAcesso verificacao;

    private static ValidatableResponse entrar(String email, String senha) {
        return given().contentType(ContentType.JSON).body(Map.of("email", email, "senha", senha))
                .when().post("/auth/login").then();
    }

    private static RequestSpecification doApp() {
        return given().header("X-Requested-With", "fincontrol");
    }

    @Test
    void loginDevolveAccessTokenECookieHttpOnly() {
        when(keycloak.login("joao@email.com", "joao12345")).thenReturn(TOKENS);

        entrar(" Joao@Email.com ", "joao12345").statusCode(200)
                .body("accessToken", equalTo("access-1"))
                .body("expiresIn", equalTo(1800))
                .body("refreshToken", nullValue())
                .header("Cache-Control", "no-store")
                .header("Set-Cookie", allOf(containsString(COOKIE + "=refresh-1"), containsString("HttpOnly"),
                        containsString("SameSite=Strict"), containsString("Path=/auth"),
                        containsString("Max-Age=28800"), containsString("Secure")));
    }

    @Test
    void loginComSenhaErradaRecebe401ComProblem() {
        when(keycloak.login("joao@email.com", "errada"))
                .thenThrow(new NaoAutenticadoException("E-mail ou senha inválidos."));

        entrar("joao@email.com", "errada").statusCode(401)
                .body("detail", equalTo("E-mail ou senha inválidos."))
                .header("Set-Cookie", nullValue());
    }

    @Test
    void loginSemCamposRecebe400SemChamarOKeycloak() {
        entrar("", "").statusCode(400);

        verifyNoInteractions(keycloak);
    }

    @Test
    void loginSemCorpoRecebe400() {
        given().contentType(ContentType.JSON).when().post("/auth/login").then().statusCode(400);
    }

    @Test
    void loginComKeycloakForaDoArRecebe503() {
        when(keycloak.login(any(), any()))
                .thenThrow(new ServicoExternoException("O serviço de login está indisponível no momento.", null));

        entrar("joao@email.com", "joao12345").statusCode(503);
    }

    @Test
    void loginEncerraASessaoAnteriorDoNavegador() {
        when(keycloak.login("joao@email.com", "joao12345")).thenReturn(TOKENS);

        given().cookie(COOKIE, "refresh-antigo").contentType(ContentType.JSON)
                .body(Map.of("email", "joao@email.com", "senha", "joao12345"))
                .when().post("/auth/login").then().statusCode(200);

        verify(keycloak).logout("refresh-antigo");
    }

    @Test
    void renovarUsaOCookieETrocaPeloNovo() {
        when(keycloak.renovar("refresh-0")).thenReturn(TOKENS);

        doApp().cookie(COOKIE, "refresh-0").when().post("/auth/refresh").then().statusCode(200)
                .body("accessToken", equalTo("access-1"))
                .header("Set-Cookie", containsString(COOKIE + "=refresh-1"));
    }

    @Test
    void renovarSemCookieRecebe401() {
        doApp().when().post("/auth/refresh").then().statusCode(401);

        verifyNoInteractions(keycloak);
    }

    @Test
    void renovarComSessaoEncerradaApagaOCookie() {
        when(keycloak.renovar("refresh-0"))
                .thenThrow(new NaoAutenticadoException("Sessão expirada. Faça login novamente."));

        doApp().cookie(COOKIE, "refresh-0").when().post("/auth/refresh").then().statusCode(401)
                .body("detail", equalTo("Sessão expirada. Faça login novamente."))
                .header("Set-Cookie", containsString("Max-Age=0"));
    }

    @Test
    void renovarComKeycloakForaDoArMantemOCookie() {
        when(keycloak.renovar("refresh-0"))
                .thenThrow(new ServicoExternoException("O serviço de login está indisponível no momento.", null));

        doApp().cookie(COOKIE, "refresh-0").when().post("/auth/refresh").then().statusCode(503)
                .header("Set-Cookie", nullValue());
    }

    @Test
    void renovarSemOCabecalhoDoAppRecebe403() {
        given().cookie(COOKIE, "refresh-0").when().post("/auth/refresh").then().statusCode(403);

        verify(keycloak, never()).renovar(any());
    }

    @Test
    void logoutEncerraASessaoEApagaOCookie() {
        doApp().cookie(COOKIE, "refresh-0").when().post("/auth/logout").then().statusCode(204)
                .header("Set-Cookie", containsString("Max-Age=0"));

        verify(keycloak).logout("refresh-0");
    }

    @Test
    void logoutRevogaASessaoNoPerfilLocal() {
        String refresh = Tokens.comClaims("{\"sub\":\"" + Usuarios.JOAO_SUB + "\",\"sid\":\"sessao-logout\"}");
        when(keycloak.logout(refresh)).thenReturn(true);

        doApp().cookie(COOKIE, refresh).when().post("/auth/logout").then().statusCode(204);

        assertEquals(RegrasAcesso.SESSAO_ENCERRADA,
                verificacao.motivoParaRecusar(Usuarios.JOAO_SUB, "sessao-logout"));
        assertNull(verificacao.motivoParaRecusar(Usuarios.JOAO_SUB, "outra-sessao"));
    }

    @Test
    void logoutRecusadoPeloKeycloakNaoRevogaNada() {
        String forjado = Tokens.comClaims("{\"sub\":\"" + Usuarios.JOAO_SUB + "\",\"sid\":\"sessao-forjada\"}");
        when(keycloak.logout(forjado)).thenReturn(false);

        doApp().cookie(COOKIE, forjado).when().post("/auth/logout").then().statusCode(204)
                .header("Set-Cookie", containsString("Max-Age=0"));

        assertNull(verificacao.motivoParaRecusar(Usuarios.JOAO_SUB, "sessao-forjada"));
    }

    @Test
    void logoutSemSessaoSoApagaOCookie() {
        doApp().when().post("/auth/logout").then().statusCode(204);

        verifyNoInteractions(keycloak);
    }

    @Test
    void logoutSemOCabecalhoDoAppRecebe403() {
        given().cookie(COOKIE, "refresh-0").when().post("/auth/logout").then().statusCode(403);

        verifyNoInteractions(keycloak);
    }
}
