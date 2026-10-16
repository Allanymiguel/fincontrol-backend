package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.CategoriaPadraoResponseDTO;
import br.unitins.tp2.model.CategoriaPadrao;

public final class CategoriaPadraoResponseMapper {

    private CategoriaPadraoResponseMapper() {
    }

    public static CategoriaPadraoResponseDTO toResponse(CategoriaPadrao categoria) {
        return new CategoriaPadraoResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getCor(),
                categoria.getArquivada());
    }
}
