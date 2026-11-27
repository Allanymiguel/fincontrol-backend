package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.testes.Tokens;

class SessaoDoTokenTest {

    @Test
    void leDonoESessaoDoToken() {
        assertEquals(new SessaoDoToken("usuario-1", "sessao-1"),
                SessaoDoToken.ler(Tokens.comClaims("{\"sub\":\"usuario-1\",\"sid\":\"sessao-1\"}")));
    }

    @Test
    void tokenSemSessaoOuIlegivelDevolveNulo() {
        assertNull(SessaoDoToken.ler(Tokens.comClaims("{\"sub\":\"usuario-1\"}")));
        assertNull(SessaoDoToken.ler("nao-e-um-token"));
        assertNull(SessaoDoToken.ler("a.%%%.c"));
        assertNull(SessaoDoToken.ler(null));
    }
}
