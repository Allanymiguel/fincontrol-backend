package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ContatoRequestDTO(
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @NotNull(message = "O tipo de contato deve ser informado.")
    Long idTipo,

    @NotBlank(message = "O CPF/CNPJ deve ser informado.")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos), somente números.")
    String documento,

    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email,

    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter DDD + número (10 ou 11 dígitos).")
    String telefone,

    @NotBlank(message = "O CEP deve ser informado.")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 dígitos.")
    String cep,

    @NotBlank(message = "O logradouro deve ser informado.")
    @Length(max = 120, message = "O logradouro deve conter no máximo 120 caracteres.")
    String logradouro,

    @NotBlank(message = "O número deve ser informado.")
    @Length(max = 10, message = "O número deve conter no máximo 10 caracteres.")
    String numero,

    @Length(max = 60, message = "O complemento deve conter no máximo 60 caracteres.")
    String complemento,

    @NotBlank(message = "O bairro deve ser informado.")
    @Length(max = 60, message = "O bairro deve conter no máximo 60 caracteres.")
    String bairro,

    @NotNull(message = "O município deve ser informado. Consulte o CEP.")
    Long idMunicipio
) {
}
