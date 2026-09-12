package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaDTO;
import br.unitins.tp2.model.Categoria;
import jakarta.validation.Valid;

public interface CategoriaService {

    Categoria create(@Valid CategoriaDTO dto);
    void update(long id, CategoriaDTO dto);
    void delete(long id);
    Categoria findById(long id);
    List<Categoria> findAll(Integer page, Integer pageSize);
    List<Categoria> findByNome(String nome, Integer page, Integer pageSize);
    long count();
    long count(String nome);

}
