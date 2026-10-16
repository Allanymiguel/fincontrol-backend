package br.unitins.tp2.dto;

import br.unitins.tp2.model.TipoTransacao;

public record CategoriaPadraoResponseDTO(
    Long id,
    String nome,
    TipoTransacao tipo,
    String cor,
    Boolean arquivada
) {
}
