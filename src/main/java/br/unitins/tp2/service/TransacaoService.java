package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.TransacaoRequestDTO;
import br.unitins.tp2.model.Transacao;
import jakarta.validation.Valid;

public interface TransacaoService {

    Transacao create(@Valid TransacaoRequestDTO transacao);
    void update(long id, TransacaoRequestDTO transacao);
    void delete(long id);
    Transacao findById(long id);
    List<Transacao> findAll(int page, int pageSize);
    List<Transacao> findByDescricao(String descricao, int page, int pageSize);
    List<Transacao> findByCategoria(Long idCategoria, int page, int pageSize);
    List<Transacao> findByDescricaoAndCategoria(String descricao, Long idCategoria, int page, int pageSize);
    long count();
    long count(String descricao);
    long countByCategoria(Long idCategoria);
    long count(String descricao, Long idCategoria);
}
