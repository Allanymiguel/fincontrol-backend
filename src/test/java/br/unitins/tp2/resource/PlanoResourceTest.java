package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.testes.Usuarios;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

// Teste de fumaca da infraestrutura do @QuarkusTest: banco do Dev Services com o seed e usuario simulado
@QuarkusTest
class PlanoResourceTest {

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminBuscaPlanoDoSeed() {
        given().when().get("/planos/1")
                .then().statusCode(200)
                .body("nome", equalTo("Essencial"))
                // o seed nao traz imagens: os planos comecam sem
                .body("imagem", nullValue());
    }

    @Test
    void semTokenRecebe401() {
        given().when().get("/planos/1")
                .then().statusCode(401);
    }
}
