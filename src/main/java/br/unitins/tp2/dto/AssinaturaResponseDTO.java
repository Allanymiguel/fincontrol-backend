package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.StatusAssinatura;

// Traz os nomes de usuario/plano e o codigo do cupom (nao so os ids) para a listagem
public record AssinaturaResponseDTO(
    Long id,
    Long idUsuario,
    String nomeUsuario,
    String emailUsuario,
    Long idPlano,
    String nomePlano,
    Long idCupom,
    String codigoCupom,
    BigDecimal percentualDesconto,
    StatusAssinatura status,
    LocalDate dataInicio,
    LocalDate dataFim,
    Periodicidade periodicidade,
    BigDecimal valorPago,
    FormaPagamento formaPagamento,
    String observacao
) {
}
