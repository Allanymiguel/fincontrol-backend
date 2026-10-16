package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Cadastro de conta pelo proprio usuario: a senha vai so para o Keycloak, nunca e salva aqui
public record RegistroRequestDTO(
    @Length(min = 2, max = 120, message = "O nome deve conter entre 2 e 120 caracteres.")
    @NotBlank(message = "O nome deve ser informado.")
    String nome,

    @NotBlank(message = "O e-mail deve ser informado.")
    @Email(message = "O e-mail informado é inválido.")
    @Length(max = 120, message = "O e-mail deve conter no máximo 120 caracteres.")
    String email,

    @NotBlank(message = "A senha deve ser informada.")
    @Length(min = 8, max = 64, message = "A senha deve conter entre 8 e 64 caracteres.")
    String senha,

    @NotBlank(message = "O CPF/CNPJ deve ser informado.")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos), somente números.")
    String documento,

    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve ter DDD + número (10 ou 11 dígitos).")
    String telefone,

    @NotNull(message = "O perfil fiscal deve ser informado.")
    Long idPerfilFiscal,

    // obrigatoria so para MEI
    Long idAtividadeMei
) {
}
