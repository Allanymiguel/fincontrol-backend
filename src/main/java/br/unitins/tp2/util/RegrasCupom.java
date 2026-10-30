package br.unitins.tp2.util;

import java.util.function.LongSupplier;

import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.Plano;

// Regras novas do cupom na troca de plano (e na simulacao), sem banco: restricao por plano e limite
// de usos. Uso = adesao da assinatura; quem ja aderiu ao cupom nao gasta outro uso.
public final class RegrasCupom {

    private RegrasCupom() {
    }

    // null = o cupom vale para esta escolha. usos so e consultado quando ha limite.
    public static String motivoParaNaoAplicar(Cupom cupom, Plano plano, boolean jaAderiu, LongSupplier usos) {
        if (!cupom.valePara(plano))
            return "O cupom " + cupom.getCodigo() + " não vale para o plano " + plano.getNome() + ". Vale para: "
                    + RetratoAuditoria.nomesDosPlanos(cupom) + ".";

        Integer limite = cupom.getLimiteUsos();
        if (limite != null && !jaAderiu && usos.getAsLong() >= limite)
            return "O cupom " + cupom.getCodigo() + " já atingiu o limite de " + limite + (limite == 1 ? " uso." : " usos.");

        return null;
    }
}
