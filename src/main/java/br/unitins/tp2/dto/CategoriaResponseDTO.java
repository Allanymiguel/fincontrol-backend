package br.unitins.tp2.dto;

import br.unitins.tp2.model.TipoTransacao;

public record CategoriaResponseDTO(
    Long id,
    String nome,
    TipoTransacao tipo,
    String cor,
    Boolean ativa
) {
}
