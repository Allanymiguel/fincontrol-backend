package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class CategoriaDTO {

    @Length(min = 2, max = 60, message = "O campo deve conter entre 2 e 60 caracteres (back).")
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String nome;

    @NotBlank(message = "O campo deve ser informado (back).")
    private final String tipo;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "O campo deve ser uma cor hexadecimal válida, ex.: #4F46E5 (back).")
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String cor;

    @NotNull(message = "O campo deve ser informado (back).")
    private final Boolean ativa;

    public CategoriaDTO(String nome, String tipo, String cor, Boolean ativa) {
        this.nome = nome;
        this.tipo = tipo;
        this.cor = cor;
        this.ativa = ativa;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCor() {
        return cor;
    }

    public Boolean getAtiva() {
        return ativa;
    }
}
