package br.unitins.tp2.mapper;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.tp2.dto.ExecucaoCobrancaResponseDTO;
import br.unitins.tp2.model.ExecucaoCobranca;

public final class ExecucaoCobrancaResponseMapper {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<List<ExecucaoCobrancaResponseDTO.Erro>> ERROS = new TypeReference<>() {
    };

    private ExecucaoCobrancaResponseMapper() {
    }

    public static ExecucaoCobrancaResponseDTO toResponse(ExecucaoCobranca execucao) {
        return new ExecucaoCobrancaResponseDTO(
                execucao.getId(),
                execucao.getDataReferencia(),
                execucao.getInicio(),
                execucao.getFim(),
                execucao.getDuracaoMs(),
                execucao.getOrigem(),
                execucao.getStatus(),
                execucao.getDisparadoPor(),
                execucao.getAssinaturasProcessadas(),
                execucao.getPagamentosGerados(),
                execucao.getValorTotal(),
                execucao.getTrocasAplicadas(),
                execucao.getVoltasAoGratuito(),
                execucao.getPagamentosRecusados(),
                execucao.getMensagemErro(),
                erros(execucao.getErros()));
    }

    private static List<ExecucaoCobrancaResponseDTO.Erro> erros(String json) {
        if (json == null || json.isBlank())
            return List.of();
        try {
            return JSON.readValue(json, ERROS);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
