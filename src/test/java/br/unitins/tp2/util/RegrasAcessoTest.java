package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

// Token valido que mesmo assim e recusado: conta desativada ou sessao encerrada pelo admin
class RegrasAcessoTest {

    @Test
    void contaAtivaSemSessaoRevogadaSegue() {
        assertNull(RegrasAcesso.motivoParaRecusar(true, null, "sessao-1"));
        assertNull(RegrasAcesso.motivoParaRecusar(true, "sessao-9", "sessao-1"));
    }

    @Test
    void contaDesativadaERecusadaNaHora() {
        assertEquals(RegrasAcesso.CONTA_DESATIVADA, RegrasAcesso.motivoParaRecusar(false, null, "sessao-1"));
    }

    @Test
    void tokenDeSessaoEncerradaERecusado() {
        assertEquals(RegrasAcesso.SESSAO_ENCERRADA,
                RegrasAcesso.motivoParaRecusar(true, "sessao-0,sessao-1", "sessao-1"));
    }

    @Test
    void tokenSemSidNaoEBarradoPorSessao() {
        assertNull(RegrasAcesso.motivoParaRecusar(true, "sessao-1", null));
    }

    @Test
    void acrescentarNaoRepeteEGuardaSoAsMaisRecentes() {
        assertEquals("a,b,c", RegrasAcesso.acrescentar("a,b", List.of("b", "c")));
        List<String> muitas = IntStream.range(0, 60).mapToObj(i -> "s" + i).toList();
        String guardadas = RegrasAcesso.acrescentar(null, muitas);
        assertEquals(RegrasAcesso.MAX_SESSOES_REVOGADAS, guardadas.split(",").length);
        assertTrue(guardadas.endsWith("s59"));
        assertFalse(guardadas.startsWith("s0,"));
    }

    @Test
    void soMotivosConhecidosVaoParaOCliente() {
        assertTrue(RegrasAcesso.isMotivoConhecido(RegrasAcesso.SESSAO_ENCERRADA));
        assertFalse(RegrasAcesso.isMotivoConhecido("Signature verification failed"));
    }
}
