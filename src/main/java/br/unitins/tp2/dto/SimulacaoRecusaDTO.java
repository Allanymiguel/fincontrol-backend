package br.unitins.tp2.dto;

import jakarta.validation.constraints.NotNull;

// liga/desliga a recusa simulada das proximas cobrancas da assinatura (nao ha gateway real, RN15)
public record SimulacaoRecusaDTO(
    @NotNull(message = "Informe se a simulação fica ligada ou desligada.")
    Boolean ligada
) {
}
