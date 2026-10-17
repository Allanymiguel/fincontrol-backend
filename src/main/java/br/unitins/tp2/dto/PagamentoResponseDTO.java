package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.StatusPagamento;

public record PagamentoResponseDTO(
    Long id,
    LocalDate dataPagamento,
    Long idPlano,
    String nomePlano,
    Periodicidade periodicidade,
    Long idCupom,
    String codigoCupom,
    BigDecimal valor,
    FormaPagamento formaPagamento,
    StatusPagamento status
) {
}
