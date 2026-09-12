package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.Transacao;

public record TransacaoResponseDTO(
    Long id,
    String descricao,
    BigDecimal valor,
    LocalDate data,
    String tipo,
    String escopo,
    CategoriaResponseDTO categoria
) {
    public static TransacaoResponseDTO valueOf(Transacao transacao) {
        return new TransacaoResponseDTO(
            transacao.getId(),
            transacao.getDescricao(),
            transacao.getValor(),
            transacao.getData(),
            transacao.getTipo().name(),
            transacao.getEscopo().name(),
            CategoriaResponseDTO.valueOf(transacao.getCategoria()));
    }
}
