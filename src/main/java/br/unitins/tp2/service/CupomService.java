package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.util.List;

import br.unitins.tp2.dto.CupomRequestDTO;
import br.unitins.tp2.model.Cupom;
import jakarta.validation.Valid;

public interface CupomService {

    // usos = adesoes (contam para o limite); pagamentos e desconto: so os aprovados
    record Metricas(long usos, long pagamentos, BigDecimal descontoConcedido) {
    }

    Cupom create(@Valid CupomRequestDTO cupom);
    void update(long id, @Valid CupomRequestDTO cupom);
    void delete(long id);
    Cupom findById(long id);
    List<Cupom> findAll(int page, int pageSize);
    List<Cupom> findByCodigo(String codigo, int page, int pageSize);
    long count();
    long count(String codigo);
    Metricas metricas(Cupom cupom);
}
