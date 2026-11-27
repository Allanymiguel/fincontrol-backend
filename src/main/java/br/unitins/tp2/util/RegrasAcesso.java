package br.unitins.tp2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Quando um token valido (assinatura e validade ok) mesmo assim deve ser recusado
public final class RegrasAcesso {

    public static final String CONTA_DESATIVADA = "Conta desativada. Fale com o administrador.";
    public static final String SESSAO_ENCERRADA = "Sua sessão foi encerrada. Faça login novamente.";

    // quantos ids de sessao revogada guardar por usuario (os mais recentes)
    public static final int MAX_SESSOES_REVOGADAS = 40;

    private RegrasAcesso() {
    }

    // mensagem que pode ir para o cliente (as demais falhas de autenticacao ficam genericas)
    public static boolean isMotivoConhecido(String mensagem) {
        return CONTA_DESATIVADA.equals(mensagem) || SESSAO_ENCERRADA.equals(mensagem);
    }

    // Conta desativada vale na hora (sem esperar o token expirar). Sessao encerrada (logout ou admin): o
    // "sid" do token esta entre as revogadas (comparar ids nao depende do relogio do Keycloak).
    // Nulo = pode seguir.
    public static String motivoParaRecusar(boolean ativo, String sessoesRevogadas, String sid) {
        if (!ativo)
            return CONTA_DESATIVADA;
        if (sid != null && sessoesRevogadas != null && separar(sessoesRevogadas).contains(sid))
            return SESSAO_ENCERRADA;
        return null;
    }

    // junta as novas sessoes revogadas as anteriores, sem repetir e guardando so as mais recentes
    public static String acrescentar(String sessoesRevogadas, Collection<String> novas) {
        Set<String> todas = new LinkedHashSet<>(sessoesRevogadas == null ? List.of() : separar(sessoesRevogadas));
        todas.addAll(novas);
        List<String> lista = new ArrayList<>(todas);
        int inicio = Math.max(0, lista.size() - MAX_SESSOES_REVOGADAS);
        return String.join(",", lista.subList(inicio, lista.size()));
    }

    private static List<String> separar(String valor) {
        return Arrays.stream(valor.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
