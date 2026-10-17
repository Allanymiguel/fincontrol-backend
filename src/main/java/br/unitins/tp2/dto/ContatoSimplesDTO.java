package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Contato simples: o minimo para anotar um cliente ou fornecedor (nome, tipo e telefone).
// CPF/CNPJ e endereco ficam para o cadastro completo, que pode ser feito depois.
public record ContatoSimplesDTO(
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @NotNull(message = "O tipo de contato deve ser informado.")
    Long idTipo,

    @NotBlank(message = "O telefone deve ser informado.")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter DDD + número (10 ou 11 dígitos).")
    String telefone,

    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email
) {
}
