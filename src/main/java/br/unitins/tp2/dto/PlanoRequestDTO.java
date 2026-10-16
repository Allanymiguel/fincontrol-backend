package br.unitins.tp2.dto;

import java.math.BigDecimal;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record PlanoRequestDTO(
    @Length(min = 2, max = 60, message = "O nome deve conter entre 2 e 60 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @NotNull(message = "O preço mensal deve ser informado.")
    @Positive(message = "O preço mensal deve ser maior que zero.")
    @Digits(integer = 12, fraction = 2, message = "O preço mensal deve ter no máximo 2 casas decimais.")
    BigDecimal precoMensal,

    @NotNull(message = "O preço anual deve ser informado.")
    @Positive(message = "O preço anual deve ser maior que zero.")
    @Digits(integer = 12, fraction = 2, message = "O preço anual deve ter no máximo 2 casas decimais.")
    BigDecimal precoAnual,

    @NotNull(message = "O limite de transações deve ser informado.")
    @PositiveOrZero(message = "O limite de transações não pode ser negativo.")
    Integer limiteTransacoes,

    @NotNull(message = "O limite de metas deve ser informado.")
    @PositiveOrZero(message = "O limite de metas não pode ser negativo.")
    Integer limiteMetas,

    @NotNull(message = "Informe se o plano libera recorrências.")
    Boolean liberaRecorrencias,

    @NotNull(message = "Informe se o plano libera o módulo fiscal.")
    Boolean liberaFiscal,

    @NotNull(message = "Informe se o plano libera exportação.")
    Boolean liberaExportacao,

    @NotNull(message = "O campo ativo deve ser informado.")
    Boolean ativo
) {
}
