package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.testes.Usuarios;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;

// Catalogo de planos: ativos, ordem por preco e o plano atual do usuario (seed: Joao no Empreendedor)
@QuarkusTest
class PlanoServiceCatalogoTest {

    static final long EMPREENDEDOR = 2;

    @Inject
    PlanoService service;

    @Inject
    PlanoRepository planoRepository;

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void usuarioVeOProprioPlanoComoAtual() {
        PlanoService.Catalogo catalogo = service.catalogo(0, 100);

        assertEquals(EMPREENDEDOR, catalogo.idPlanoAtual());
        assertTrue(ids(catalogo).contains(EMPREENDEDOR));
    }

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void planoInativoFicaForaDoCatalogo() {
        long inativo = salvar(plano(false));

        assertFalse(ids(service.catalogo(0, 100)).contains(inativo));
    }

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void planoAtualInativoContinuaAparecendoParaODono() {
        QuarkusTransaction.requiringNew().run(() -> planoRepository.findById(EMPREENDEDOR).setAtivo(false));
        try {
            assertTrue(ids(service.catalogo(0, 100)).contains(EMPREENDEDOR));
        } finally {
            QuarkusTransaction.requiringNew().run(() -> planoRepository.findById(EMPREENDEDOR).setAtivo(true));
        }
    }

    @Test
    @TestSecurity(user = Usuarios.JOAO, roles = "USER")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.JOAO_SUB))
    void catalogoOrdenadoPeloPrecoMensal() {
        List<Plano> planos = service.catalogo(0, 100).planos();

        for (int i = 1; i < planos.size(); i++)
            assertTrue(planos.get(i - 1).getPrecoMensal().compareTo(planos.get(i).getPrecoMensal()) <= 0);
        assertEquals("Gratuito", planos.get(0).getNome());
    }

    @Test
    @TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
    @OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
    void adminNaoTemPlanoAtual() {
        PlanoService.Catalogo catalogo = service.catalogo(0, 100);

        assertNull(catalogo.idPlanoAtual());
        assertTrue(catalogo.total() >= 3);
    }

    private static List<Long> ids(PlanoService.Catalogo catalogo) {
        return catalogo.planos().stream().map(Plano::getId).toList();
    }

    private long salvar(Plano plano) {
        QuarkusTransaction.requiringNew().run(() -> planoRepository.persist(plano));
        return plano.getId();
    }

    private static Plano plano(boolean ativo) {
        Plano plano = new Plano();
        plano.setNome("Teste " + UUID.randomUUID().toString().substring(0, 8));
        plano.setPrecoMensal(new BigDecimal("1.00"));
        plano.setPrecoAnual(new BigDecimal("10.00"));
        plano.setLimiteTransacoes(10);
        plano.setLimiteMetas(1);
        plano.setLiberaRecorrencias(false);
        plano.setLiberaFiscal(false);
        plano.setLiberaExportacao(false);
        plano.setAtivo(ativo);
        return plano;
    }
}
