package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.unitins.tp2.testes.Usuarios;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

// Quem acessa o que: sem token 401, sem o papel 403 (um endpoint de listagem de cada recurso)
@QuarkusTest
class ControleDeAcessoTest {

    @ParameterizedTest
    @ValueSource(strings = { "/auth/me", "/transacoes", "/categorias", "/contatos", "/planos/catalogo", "/planos",
            "/cupons", "/categorias-padrao", "/parametros-fiscais", "/faixas-irpf", "/admin/usuarios",
            "/admin/dashboard", "/admin/auditoria", "/admin/ceps", "/admin/cobrancas/resumo", "/admin/pagamentos",
            "/admin/usuarios/2/assinatura" })
    void semTokenRecebe401(String caminho) {
        given().when().get(caminho).then().statusCode(401).body("title", equalTo("Não autenticado"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/planos", "/cupons", "/categorias-padrao", "/parametros-fiscais", "/faixas-irpf",
            "/admin/usuarios", "/admin/dashboard", "/admin/auditoria", "/admin/ceps", "/admin/cobrancas/resumo",
            "/admin/pagamentos", "/admin/usuarios/2/assinatura" })
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void usuarioNaAreaDoAdminRecebe403(String caminho) {
        given().when().get(caminho).then().statusCode(403).body("title", equalTo("Acesso negado"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "/transacoes", "/categorias", "/contatos" })
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminNosDadosPessoaisRecebe403(String caminho) {
        given().when().get(caminho).then().statusCode(403).body("title", equalTo("Acesso negado"));
    }

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void usuarioConsultaOProprioPerfil() {
        given().when().get("/auth/me").then().statusCode(200).body("email", equalTo(Usuarios.JOAO));
    }

    @Test
    void consultasDoCadastroSaoPublicas() {
        given().queryParam("uf", "TO").when().get("/enderecos-cliente/municipios").then().statusCode(200);
    }
}
