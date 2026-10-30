package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.StatusPagamento;

// Cobranca da assinatura (relacao do usuario com o FinControl). Identifica o usuario so por nome e
// e-mail: nada de documento ou telefone.
public record PagamentoResponseDTO(
    Long id,
    LocalDate dataPagamento,
    Long idUsuario,
    String nomeUsuario,
    String emailUsuario,
    Long idPlano,
    String nomePlano,
    Periodicidade periodicidade,
    Long idCupom,
    String codigoCupom,
    BigDecimal valor,
    // quanto o cupom tirou do preco cheio nesta cobranca (zero sem cupom)
    BigDecimal valorDesconto,
    FormaPagamento formaPagamento,
    StatusPagamento status,
    // nulos se nao foi estornado
    LocalDateTime dataEstorno,
    String motivoEstorno,
    String estornadoPor
) {

    // resumo da lista de pagamentos: quantidade e soma de um status
    public record TotalPorStatus(StatusPagamento status, long quantidade, BigDecimal valor) {
    }
}
