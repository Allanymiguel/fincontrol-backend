package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;

// Restricao por plano e limite de usos do cupom (uso = adesao)
class RegrasCupomTest {

    private final Plano essencial = plano("Essencial", "19.90", "199.00");
    private final Plano empreendedor = plano("Empreendedor", "39.90", "399.00");

    @Test
    void cupomSemRestricaoESemLimiteValeParaTodos() {
        Cupom cupom = cupom("BEMVINDO10", null);

        assertNull(RegrasCupom.motivoParaNaoAplicar(cupom, essencial, false, () -> {
            throw new AssertionError("sem limite, nao conta usos");
        }));
        assertNull(RegrasCupom.motivoParaNaoAplicar(cupom, empreendedor, false, () -> 999));
    }

    @Test
    void cupomRestritoNaoValeParaOutroPlano() {
        Cupom cupom = cupom("PARCEIRO", null);
        cupom.getPlanos().add(empreendedor);

        assertNull(RegrasCupom.motivoParaNaoAplicar(cupom, empreendedor, false, () -> 0));
        assertEquals("O cupom PARCEIRO não vale para o plano Essencial. Vale para: Empreendedor.",
                RegrasCupom.motivoParaNaoAplicar(cupom, essencial, false, () -> 0));
    }

    @Test
    void limiteAtingidoBloqueiaNovaAdesao() {
        Cupom cupom = cupom("PARCEIRO", 1);

        assertNull(RegrasCupom.motivoParaNaoAplicar(cupom, essencial, false, () -> 0));
        assertEquals("O cupom PARCEIRO já atingiu o limite de 1 uso.",
                RegrasCupom.motivoParaNaoAplicar(cupom, essencial, false, () -> 1));
    }

    @Test
    void quemJaAderiuNaoGastaOutroUso() {
        Cupom cupom = cupom("BLACKFRIDAY", 100);

        assertNull(RegrasCupom.motivoParaNaoAplicar(cupom, essencial, true, () -> 100));
    }

    @Test
    void descontoDoPagamentoEOPrecoCheioMenosOCobrado() {
        Cupom cupom = cupom("BLACKFRIDAY", 100);
        cupom.setPercentualDesconto(new BigDecimal("30.00"));

        BigDecimal cobrado = essencial.calcularValor(Periodicidade.MENSAL, cupom);
        BigDecimal cheio = essencial.calcularValor(Periodicidade.MENSAL, null);

        assertEquals(new BigDecimal("13.93"), cobrado);
        assertEquals(new BigDecimal("5.97"), cheio.subtract(cobrado));
    }

    private static Plano plano(String nome, String mensal, String anual) {
        Plano plano = new Plano();
        plano.setNome(nome);
        plano.setPrecoMensal(new BigDecimal(mensal));
        plano.setPrecoAnual(new BigDecimal(anual));
        plano.setAtivo(true);
        return plano;
    }

    private static Cupom cupom(String codigo, Integer limite) {
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo);
        cupom.setPercentualDesconto(new BigDecimal("10.00"));
        cupom.setValidade(LocalDate.of(2026, 12, 31));
        cupom.setAtivo(true);
        cupom.setLimiteUsos(limite);
        return cupom;
    }
}
