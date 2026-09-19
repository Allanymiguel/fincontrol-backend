package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.CategoriaResponseDTO;
import br.unitins.tp2.model.Categoria;

public final class CategoriaResponseMapper {

    private CategoriaResponseMapper() {
    }

    public static CategoriaResponseDTO toResponse(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getTipo(),
                categoria.getCor(),
                categoria.getAtiva());
    }
}
