package br.unitins.tp2.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import br.unitins.tp2.model.StatusExecucaoCobranca;
import br.unitins.tp2.model.StatusPagamento;

// Regras da cobranca sem banco (testaveis isoladamente): historico de execucoes, recusa e estorno
public final class RegrasCobranca {

    // pagamento recusado: nova tentativa a cada cobranca; na 3a recusa seguida volta ao Gratuito
    public static final int MAX_TENTATIVAS = 3;

    private RegrasCobranca() {
    }

    // falha da execucao inteira ou de todas as assinaturas = FALHA; de algumas = PARCIAL; nenhuma = SUCESSO.
    // Pagamento recusado nao e erro da execucao: a cobranca rodou e registrou a recusa.
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

    // tentativa = numero desta recusa (1, 2, 3...)
    public static boolean esgotouTentativas(int tentativa) {
        return tentativa >= MAX_TENTATIVAS;
    }

    // texto da tentativa no historico da assinatura (e na linha do tempo do usuario)
    public static String detalheRecusa(int tentativa, BigDecimal valor) {
        String texto = "Tentativa " + tentativa + " de " + MAX_TENTATIVAS + ": R$ "
                + valor.toPlainString().replace('.', ',') + " recusado.";
        return esgotouTentativas(tentativa) ? texto + " Volta ao Gratuito."
                : texto + " O plano continua; nova tentativa na próxima cobrança.";
    }

    // so pagamento aprovado pode ser estornado; null = pode
    public static String motivoParaNaoEstornar(StatusPagamento status) {
        return switch (status) {
            case APROVADO -> null;
            case ESTORNADO -> "Este pagamento já foi estornado.";
            case RECUSADO -> "Pagamento recusado não foi recebido: não há o que estornar.";
            default -> "Só pagamento aprovado pode ser estornado.";
        };
    }
}
