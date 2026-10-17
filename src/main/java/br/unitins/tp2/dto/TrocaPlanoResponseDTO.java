package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.model.EfeitoTroca;

// Resultado (ou previa, na simulacao) de uma troca de plano
public record TrocaPlanoResponseDTO(
    EfeitoTroca efeito,
    // saldo nao usado do periodo atual que vira dias do plano novo (troca imediata)
    BigDecimal credito,
    // quantos dias do plano novo o credito paga (troca imediata); 0 nos outros casos
    long diasCobertos,
    // cobrado hoje: so quando nao ha credito para cobrir o plano novo (ex.: saindo do Gratuito)
    BigDecimal valorCobradoAgora,
    // quando o plano novo comeca a ser cobrado (ou a troca agendada passa a valer); null = sem cobranca
    LocalDate dataProximaCobranca,
    BigDecimal valorProximaCobranca,
    // a assinatura depois da troca (na simulacao, como esta hoje)
    AssinaturaResponseDTO assinatura
) {
}
