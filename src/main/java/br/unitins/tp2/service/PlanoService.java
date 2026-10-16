package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.model.Plano;
import jakarta.validation.Valid;

public interface PlanoService {

    Plano create(@Valid PlanoRequestDTO plano);
    void update(long id, @Valid PlanoRequestDTO plano);
    void delete(long id);
    Plano findById(long id);
    List<Plano> findAll(int page, int pageSize);
    List<Plano> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
