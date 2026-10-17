package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Cadastro simples: so o acesso (nome, e-mail e senha). Os dados fiscais e o endereco podem ser
// completados depois pelo admin. A senha vai so para o Keycloak, nunca e salva aqui.
public record CadastroSimplesDTO(
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

    // so o admin escolhe (1 = USER, 2 = ADMIN); no cadastro publico e sempre USER
    Long idPapel
) {
}
