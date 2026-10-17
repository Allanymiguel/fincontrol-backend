package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ContatoCompletoDTO;
import br.unitins.tp2.dto.ContatoSimplesDTO;
import br.unitins.tp2.model.Contato;
import jakarta.validation.Valid;

public interface ContatoService {

    Contato createSimples(@Valid ContatoSimplesDTO contato);
    Contato createCompleto(@Valid ContatoCompletoDTO contato);
    void updateSimples(long id, @Valid ContatoSimplesDTO contato);
    void updateCompleto(long id, @Valid ContatoCompletoDTO contato);
    void delete(long id);
    Contato findById(long id);
    List<Contato> findAll(int page, int pageSize);
    List<Contato> findByNome(String nome, int page, int pageSize);
    long count();
    long count(String nome);
}
