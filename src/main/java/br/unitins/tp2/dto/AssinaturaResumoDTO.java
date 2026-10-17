package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.Periodicidade;

// O essencial da assinatura para a listagem de usuarios do admin
public record AssinaturaResumoDTO(
    Long idPlano,
    String nomePlano,
    Periodicidade periodicidade,
    // preco da proxima cobranca do plano vigente
    BigDecimal valor,
    // null no plano gratuito (nao ha cobranca)
    LocalDate dataProximaCobranca,
    // plano que passa a valer na proxima cobranca; nulos se nao houver troca agendada
    Long idPlanoAgendado,
    String nomePlanoAgendado
) {
}
