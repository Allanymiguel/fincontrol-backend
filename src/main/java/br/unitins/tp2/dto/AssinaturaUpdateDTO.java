package br.unitins.tp2.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

// Campos editaveis depois de criada. Usuario, plano, cupom e valor nao mudam:
// para trocar de plano, cancela-se esta e cria-se outra (o historico fica).
public record AssinaturaUpdateDTO(
    @NotNull(message = "O status deve ser informado.")
    Long idStatus,

    // nula = vigente, sem prazo
    LocalDate dataFim,

    @Length(max = 255, message = "A observação deve conter no máximo 255 caracteres.")
    String observacao
) {
}
