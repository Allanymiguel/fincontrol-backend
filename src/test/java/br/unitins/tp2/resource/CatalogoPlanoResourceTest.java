package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.testes.Usuarios;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

// GET /planos/catalogo (seed: Joao no Empreendedor, id 2)
@QuarkusTest
class CatalogoPlanoResourceTest {

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void usuarioRecebeOProprioPlanoComoAtual() {
        given().queryParam("pageSize", 100).when().get("/planos/catalogo")
                .then().statusCode(200)
                .header("X-Total-Count", notNullValue())
                .body("items.find { it.plano.id == 2 }.atual", equalTo(true))
                .body("items.findAll { it.plano.id != 2 }.atual", everyItem(equalTo(false)))
                .body("items.plano.nome", hasItem("Gratuito"));
    }

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminRecebeTodosComAtualFalse() {
        given().queryParam("pageSize", 100).when().get("/planos/catalogo")
                .then().statusCode(200)
                .body("items.atual", everyItem(equalTo(false)))
                .body("items.plano.nome", hasItem("Empreendedor"));
    }

    @Test
    void semTokenRecebe401() {
        given().when().get("/planos/catalogo").then().statusCode(401);
    }
}
