package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.EscopoTransacao;
import br.unitins.tp2.model.TipoTransacao;

public record TransacaoResponseDTO(
    Long id,
    String descricao,
    BigDecimal valor,
    LocalDate data,
    TipoTransacao tipo,
    EscopoTransacao escopo,
    CategoriaResponseDTO categoria
) {
}
