package br.unitins.tp2.security;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;

// Cookie com o refresh token: HttpOnly (o JavaScript nao le), SameSite=Strict (outro site nao o
// envia) e restrito a /auth (so vai nas chamadas de sessao)
@ApplicationScoped
public class CookieSessao {

    public static final String NOME = "fincontrol_refresh";

    // false so em desenvolvimento, sem HTTPS
    @ConfigProperty(name = "fincontrol.auth.cookie-secure")
    boolean seguro;

    public NewCookie criar(String refreshToken, long duracaoEmSegundos) {
        return montar(refreshToken, Math.toIntExact(duracaoEmSegundos));
    }

    // mesmo nome e caminho com duracao zero: o navegador apaga o cookie
    public NewCookie apagar() {
        return montar("", 0);
    }

    private NewCookie montar(String valor, int duracao) {
        return new NewCookie.Builder(NOME)
                .value(valor)
                .path("/auth")
                .maxAge(duracao)
                .httpOnly(true)
                .secure(seguro)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
