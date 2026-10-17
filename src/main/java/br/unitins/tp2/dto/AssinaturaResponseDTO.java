package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.StatusAssinatura;

// A assinatura (unica) do usuario: plano vigente, proxima cobranca e a troca agendada, se houver
public record AssinaturaResponseDTO(
    Long id,
    Long idUsuario,
    String nomeUsuario,
    String emailUsuario,
    Long idPlano,
    String nomePlano,
    Periodicidade periodicidade,
    Long idCupom,
    String codigoCupom,
    BigDecimal percentualDesconto,
    FormaPagamento formaPagamento,
    StatusAssinatura status,
    LocalDate dataInicio,
    // preco de cada cobranca do plano vigente
    BigDecimal valor,
    LocalDate dataInicioPeriodo,
    // null no plano gratuito (nao ha cobranca)
    LocalDate dataProximaCobranca,
    // null se nao houver troca agendada para a proxima cobranca
    TrocaAgendada trocaAgendada,
    String observacao
) {

    public record TrocaAgendada(
        Long idPlano,
        String nomePlano,
        Periodicidade periodicidade,
        String codigoCupom,
        FormaPagamento formaPagamento,
        // valor que sera cobrado na data, ja com o cupom
        BigDecimal valor,
        LocalDate data
    ) {
    }
}
