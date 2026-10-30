package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.StatusExecucaoCobranca;

// Historico das execucoes da cobranca
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
}
