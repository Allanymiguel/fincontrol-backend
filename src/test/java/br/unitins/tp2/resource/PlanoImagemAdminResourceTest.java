package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.service.ArmazenamentoService;
import br.unitins.tp2.testes.Usuarios;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;

// PUT e DELETE da imagem do plano: so ADMIN, validacao e limite do corpo (S3 simulado)
@QuarkusTest
class PlanoImagemAdminResourceTest {

    static final byte[] PNG = Arrays.copyOf(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A }, 32);

    @Inject
    PlanoRepository planoRepository;

    @InjectMock
    ArmazenamentoService armazenamento;

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminEnviaAImagemERecebeOPlanoSemAChave() {
        long id = novoPlano();

        given().multiPart("arquivo", "logo.png", PNG, "image/png")
                .when().put("/planos/{id}/imagem", id)
                .then().statusCode(200)
                .body("id", equalTo((int) id))
                .body("imagem.nome", equalTo("logo.png"))
                .body("imagem.tipo", equalTo("image/png"))
                .body("imagem.tamanho", equalTo(PNG.length))
                .body("imagem.versao", matchesPattern("[0-9a-f-]{36}"))
                .body(not(containsString("planos/")));

        verify(armazenamento).enviar(anyString(), eq(PNG), eq("image/png"));
    }

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminRemoveAImagemCom204() {
        long id = novoPlano();
        given().multiPart("arquivo", "logo.png", PNG, "image/png").when().put("/planos/{id}/imagem", id)
                .then().statusCode(200);

        given().when().delete("/planos/{id}/imagem", id).then().statusCode(204);

        verify(armazenamento).excluirSemFalhar(anyString());
    }

    @Test
    void semTokenRecebe401() {
        long id = novoPlano();

        given().multiPart("arquivo", "logo.png", PNG, "image/png").when().put("/planos/{id}/imagem", id)
                .then().statusCode(401);
        given().when().delete("/planos/{id}/imagem", id).then().statusCode(401);
    }

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void usuarioRecebe403() {
        long id = novoPlano();

        given().multiPart("arquivo", "logo.png", PNG, "image/png").when().put("/planos/{id}/imagem", id)
                .then().statusCode(403);
        given().when().delete("/planos/{id}/imagem", id).then().statusCode(403);
        verify(armazenamento, never()).enviar(anyString(), any(), anyString());
    }

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void arquivoInvalidoOuSemAParteArquivoRecebe400() {
        long id = novoPlano();
        byte[] texto = "isto nao e uma imagem".getBytes(StandardCharsets.UTF_8);

        given().multiPart("arquivo", "foto.png", texto, "image/png").when().put("/planos/{id}/imagem", id)
                .then().statusCode(400)
                .body("errors[0].field", equalTo("arquivo"))
                .body("errors[0].message", equalTo("Envie uma imagem JPEG, PNG ou WebP."));

        given().multiPart("outro", "x").when().put("/planos/{id}/imagem", id)
                .then().statusCode(400)
                .body("errors[0].field", equalTo("arquivo"))
                .body("errors[0].message", equalTo("Envie a imagem do plano."));

        verify(armazenamento, never()).enviar(anyString(), any(), anyString());
    }

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void corpoAcimaDe3MBRecebe413() {
        long id = novoPlano();
        byte[] grande = Arrays.copyOf(PNG, 3_500_000);

        given().multiPart("arquivo", "grande.png", grande, "image/png").when().put("/planos/{id}/imagem", id)
                .then().statusCode(413);

        verify(armazenamento, never()).enviar(anyString(), any(), anyString());
    }

    private long novoPlano() {
        Plano plano = PlanoImagemResourceTest.novoPlano(null);
        QuarkusTransaction.requiringNew().run(() -> planoRepository.persist(plano));
        return plano.getId();
    }
}
