package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Cupom com as metricas (so admin): usos = adesoes (contam para o limite); pagamentos e desconto
// concedido saem dos pagamentos aprovados (estornados nao contam)
public record CupomResponseDTO(
    Long id,
    String codigo,
    BigDecimal percentualDesconto,
    LocalDate validade,
    Boolean ativo,
    // nulo = sem limite
    Integer limiteUsos,
    // vazio = todos os planos
    List<PlanoDoCupom> planos,
    long usos,
    long pagamentos,
    BigDecimal descontoConcedido
) {

    public record PlanoDoCupom(Long id, String nome) {
    }
}
