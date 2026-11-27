package br.unitins.tp2.testes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

// Token no formato JWT so com o payload, para os testes (a assinatura nao e conferida por quem o le)
public final class Tokens {

    private Tokens() {
    }

    public static String comClaims(String json) {
        return "cabecalho." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8)) + ".assinatura";
    }
}
