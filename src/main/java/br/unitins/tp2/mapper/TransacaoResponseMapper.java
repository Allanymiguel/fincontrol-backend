package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.TransacaoResponseDTO;
import br.unitins.tp2.model.Transacao;

public final class TransacaoResponseMapper {

    private TransacaoResponseMapper() {
    }

    public static TransacaoResponseDTO toResponse(Transacao transacao) {
        return new TransacaoResponseDTO(
                transacao.getId(),
                transacao.getDescricao(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getTipo(),
                transacao.getEscopo(),
                CategoriaResponseMapper.toResponse(transacao.getCategoria()));
    }
}
