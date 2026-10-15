package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ContatoRequestDTO;
import br.unitins.tp2.model.Contato;
import jakarta.validation.Valid;

public interface ContatoService {

    Contato create(@Valid ContatoRequestDTO contato);
    void update(long id, @Valid ContatoRequestDTO contato);
    void delete(long id);
    Contato findById(long id);
    List<Contato> findAll(int page, int pageSize);
    List<Contato> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
