package br.unitins.tp2.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.tp2.dto.AlteracaoAuditoriaDTO;

// "Antes e depois" de uma acao do admin: compara dois retratos (campo -> valor ja formatado) e
// guarda so os campos que mudaram. Criacao = retrato "antes" vazio; exclusao = "depois" vazio.
// Documento e telefone nunca vao para a auditoria: aparecem so como "alterado" (sem valores).
public final class AlteracoesAuditoria {

    public static final String DOCUMENTO = "Documento";
    public static final String TELEFONE = "Telefone";
    private static final Set<String> SENSIVEIS = Set.of(DOCUMENTO, TELEFONE);

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<AlteracaoAuditoriaDTO>> LISTA = new TypeReference<>() {
    };

    private AlteracoesAuditoria() {
    }

    public static List<AlteracaoAuditoriaDTO> comparar(Map<String, String> antes, Map<String, String> depois) {
        Set<String> campos = new LinkedHashSet<>(antes.keySet());
        campos.addAll(depois.keySet());

        List<AlteracaoAuditoriaDTO> alteracoes = new ArrayList<>();
        for (String campo : campos) {
            String valorAntes = antes.get(campo);
            String valorDepois = depois.get(campo);
            if (Objects.equals(valorAntes, valorDepois))
                continue;
            if (SENSIVEIS.contains(campo))
                alteracoes.add(new AlteracaoAuditoriaDTO(campo, null, null, true));
            else
                alteracoes.add(new AlteracaoAuditoriaDTO(campo, valorAntes, valorDepois, false));
        }
        return alteracoes;
    }

    public static String paraJson(List<AlteracaoAuditoriaDTO> alteracoes) {
        try {
            return JSON.writeValueAsString(alteracoes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível registrar as alterações na auditoria.", e);
        }
    }

    public static List<AlteracaoAuditoriaDTO> deJson(String json) {
        if (json == null || json.isBlank())
            return List.of();
        try {
            return JSON.readValue(json, LISTA);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Alterações da auditoria ilegíveis.", e);
        }
    }
}
