package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ParametroFiscalRequestDTO;
import br.unitins.tp2.model.ParametroFiscal;
import jakarta.validation.Valid;

public interface ParametroFiscalService {

    ParametroFiscal create(@Valid ParametroFiscalRequestDTO parametro);
    void update(long id, @Valid ParametroFiscalRequestDTO parametro);
    void delete(long id);
    ParametroFiscal findById(long id);
    List<ParametroFiscal> findAll(int page, int pageSize);
    List<ParametroFiscal> findByChave(String chave, int page, int pageSize);
    long count();
    long count(String chave);
}
