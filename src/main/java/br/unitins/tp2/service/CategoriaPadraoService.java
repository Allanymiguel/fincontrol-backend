package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaPadraoRequestDTO;
import br.unitins.tp2.model.CategoriaPadrao;
import jakarta.validation.Valid;

public interface CategoriaPadraoService {

    CategoriaPadrao create(@Valid CategoriaPadraoRequestDTO categoria);
    void update(long id, @Valid CategoriaPadraoRequestDTO categoria);
    CategoriaPadrao alternarArquivada(long id);
    CategoriaPadrao findById(long id);
    List<CategoriaPadrao> findByFiltros(String nome, Long idTipo, int page, int pageSize);
    long count(String nome, Long idTipo);
}
