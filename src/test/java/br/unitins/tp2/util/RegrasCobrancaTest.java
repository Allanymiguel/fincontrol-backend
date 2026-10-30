package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.StatusExecucaoCobranca;
import br.unitins.tp2.model.StatusPagamento;

// Historico das execucoes da cobranca, recusa simulada e estorno
class RegrasCobrancaTest {

    @Test
    void semErroESucessoMesmoSemAssinaturas() {
        assertEquals(StatusExecucaoCobranca.SUCESSO, RegrasCobranca.status(0, 0, false));
        assertEquals(StatusExecucaoCobranca.SUCESSO, RegrasCobranca.status(5, 0, false));
    }

    @Test
    void algumasAssinaturasComErroEParcial() {
        assertEquals(StatusExecucaoCobranca.PARCIAL, RegrasCobranca.status(5, 2, false));
    }

    @Test
    void todasComErroOuFalhaGeralEFalha() {
        assertEquals(StatusExecucaoCobranca.FALHA, RegrasCobranca.status(3, 3, false));
        assertEquals(StatusExecucaoCobranca.FALHA, RegrasCobranca.status(0, 0, true));
        assertEquals(StatusExecucaoCobranca.FALHA, RegrasCobranca.status(4, 1, true));
    }

    @Test
    void diasSemExecucaoAutomatica() {
        List<LocalDate> rodou = List.of(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 27));

        assertEquals(List.of(LocalDate.of(2026, 10, 25), LocalDate.of(2026, 10, 26)),
                RegrasCobranca.diasSemExecucao(LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 27), rodou));
    }

    @Test
    void periodoTodoComExecucaoNaoTemDiasSemExecucao() {
        LocalDate dia = LocalDate.of(2026, 10, 29);
        assertEquals(List.of(), RegrasCobranca.diasSemExecucao(dia, dia, List.of(dia)));
    }

    @Test
    void terceiraRecusaSeguidaEsgotaAsTentativas() {
        assertFalse(RegrasCobranca.esgotouTentativas(1));
        assertFalse(RegrasCobranca.esgotouTentativas(2));
        assertTrue(RegrasCobranca.esgotouTentativas(3));
    }

    @Test
    void detalheDaRecusaDizATentativaEOQueAcontece() {
        BigDecimal valor = new BigDecimal("39.90");
        assertEquals("Tentativa 2 de 3: R$ 39,90 recusado. O plano continua; nova tentativa na próxima cobrança.",
                RegrasCobranca.detalheRecusa(2, valor));
        assertEquals("Tentativa 3 de 3: R$ 39,90 recusado. Volta ao Gratuito.", RegrasCobranca.detalheRecusa(3, valor));
    }

    @Test
    void soPagamentoAprovadoPodeSerEstornado() {
        assertNull(RegrasCobranca.motivoParaNaoEstornar(StatusPagamento.APROVADO));
        assertNotNull(RegrasCobranca.motivoParaNaoEstornar(StatusPagamento.ESTORNADO));
        assertNotNull(RegrasCobranca.motivoParaNaoEstornar(StatusPagamento.RECUSADO));
        assertNotNull(RegrasCobranca.motivoParaNaoEstornar(StatusPagamento.PENDENTE));
    }
}
