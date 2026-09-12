package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class TransacaoDTO {

    @Length(min = 2, max = 120, message = "O campo deve conter entre 2 e 120 caracteres (back).")
    @NotBlank(message = "O campo deve ser informado (back).")
    private final String descricao;

    @DecimalMin(value = "0.0", inclusive = false, message = "O campo deve ser maior que zero (back).")
    @NotNull(message = "O campo deve ser informado (back).")
    private final BigDecimal valor;

    @NotNull(message = "O campo deve ser informado (back).")
    private final LocalDate data;

    @NotBlank(message = "O campo deve ser informado (back).")
    private final String tipo;

    @NotBlank(message = "O campo deve ser informado (back).")
    private final String escopo;

    @NotNull(message = "O campo deve ser informado (back).")
    private final Long idCategoria;

    public TransacaoDTO(String descricao, BigDecimal valor, LocalDate data, String tipo, String escopo,
            Long idCategoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.escopo = escopo;
        this.idCategoria = idCategoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEscopo() {
        return escopo;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }
}
