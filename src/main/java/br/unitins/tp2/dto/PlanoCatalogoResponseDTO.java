package br.unitins.tp2.dto;

// Item do catalogo de planos: atual = plano da assinatura do usuario logado (sempre false para o admin)
public record PlanoCatalogoResponseDTO(
    PlanoResponseDTO plano,
    boolean atual
) {
}
