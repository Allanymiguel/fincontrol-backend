package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.FaixaIrpfRequestDTO;
import br.unitins.tp2.model.FaixaIrpf;
import jakarta.validation.Valid;

public interface FaixaIrpfService {

    FaixaIrpf create(@Valid FaixaIrpfRequestDTO faixa);
    void update(long id, @Valid FaixaIrpfRequestDTO faixa);
    void delete(long id);
    FaixaIrpf findById(long id);
    List<FaixaIrpf> findAll(int page, int pageSize);
    List<FaixaIrpf> findByVigencia(LocalDate vigenciaInicio, int page, int pageSize);
    List<LocalDate> findVigencias();
    long count();
    long count(LocalDate vigenciaInicio);
}
