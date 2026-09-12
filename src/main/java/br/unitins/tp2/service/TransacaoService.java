package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.TransacaoDTO;
import br.unitins.tp2.model.Transacao;
import jakarta.validation.Valid;

public interface TransacaoService {

    Transacao create(@Valid TransacaoDTO dto);
    void update(long id, TransacaoDTO dto);
    void delete(long id);
    Transacao findById(long id);
    List<Transacao> findAll(Integer page, Integer pageSize);
    List<Transacao> findByDescricao(String descricao, Integer page, Integer pageSize);
    List<Transacao> findByCategoria(Long idCategoria, Integer page, Integer pageSize);
    long count();
    long count(String descricao);

}
