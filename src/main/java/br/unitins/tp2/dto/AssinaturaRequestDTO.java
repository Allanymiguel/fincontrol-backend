package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

// Criacao de assinatura pelo admin. O valor pago nao vem do cliente: e calculado
// pelo preco do plano na periodicidade, com o desconto do cupom (se houver).
public record AssinaturaRequestDTO(
    @NotNull(message = "O usuário deve ser informado.")
    Long idUsuario,

    @NotNull(message = "O plano deve ser informado.")
    Long idPlano,

    // opcional
    Long idCupom,

    @NotNull(message = "A periodicidade deve ser informada.")
    Long idPeriodicidade,

    @NotNull(message = "A forma de pagamento deve ser informada.")
    Long idFormaPagamento,

    @Length(max = 255, message = "A observação deve conter no máximo 255 caracteres.")
    String observacao
) {
}
