package br.unitins.tp2.dto;

import jakarta.validation.constraints.NotNull;

// Plano escolhido pelo admin para o usuario. A forma de pagamento so e exigida quando o novo
// plano tem custo (se vier vazia, vale a forma ja usada nas cobrancas).
public record TrocaPlanoDTO(
    @NotNull(message = "Selecione o plano.")
    Long idPlano,

    @NotNull(message = "Selecione a periodicidade.")
    Long idPeriodicidade,

    Long idCupom,

    Long idFormaPagamento
) {
}
