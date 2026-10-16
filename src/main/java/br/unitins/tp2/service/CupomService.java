package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CupomRequestDTO;
import br.unitins.tp2.model.Cupom;
import jakarta.validation.Valid;

public interface CupomService {

    Cupom create(@Valid CupomRequestDTO cupom);
    void update(long id, @Valid CupomRequestDTO cupom);
    void delete(long id);
    Cupom findById(long id);
    List<Cupom> findAll(int page, int pageSize);
    List<Cupom> findByCodigo(String codigo, int page, int pageSize);
    long count();
    long count(String codigo);
}
