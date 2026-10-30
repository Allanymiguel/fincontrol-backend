package br.unitins.tp2.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.unitins.tp2.model.StatusExecucaoCobranca;

// Regras do historico de execucoes da cobranca, sem banco (testaveis isoladamente)
public final class RegrasCobranca {

    private RegrasCobranca() {
    }

    // falha da execucao inteira ou de todas as assinaturas = FALHA; de algumas = PARCIAL; nenhuma = SUCESSO
    public static StatusExecucaoCobranca status(int assinaturas, int comErro, boolean falhaGeral) {
        if (falhaGeral)
            return StatusExecucaoCobranca.FALHA;
        if (comErro == 0)
            return StatusExecucaoCobranca.SUCESSO;
        return comErro >= assinaturas ? StatusExecucaoCobranca.FALHA : StatusExecucaoCobranca.PARCIAL;
    }

    // dias de inicio a fim (inclusive) sem nenhuma execucao automatica
    public static List<LocalDate> diasSemExecucao(LocalDate inicio, LocalDate fim, Collection<LocalDate> diasComAutomatica) {
        List<LocalDate> dias = new ArrayList<>();
        for (LocalDate dia = inicio; !dia.isAfter(fim); dia = dia.plusDays(1)) {
            if (!diasComAutomatica.contains(dia))
                dias.add(dia);
        }
        return dias;
    }
}
