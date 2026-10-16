package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.AssinaturaRequestDTO;
import br.unitins.tp2.dto.AssinaturaUpdateDTO;
import br.unitins.tp2.model.Assinatura;
import jakarta.validation.Valid;

public interface AssinaturaService {

    Assinatura create(@Valid AssinaturaRequestDTO assinatura);
    void update(long id, @Valid AssinaturaUpdateDTO assinatura);
    Assinatura cancelar(long id);
    Assinatura findById(long id);
    List<Assinatura> findByFiltros(String busca, Long idStatus, int page, int pageSize);
    long count(String busca, Long idStatus);
}
