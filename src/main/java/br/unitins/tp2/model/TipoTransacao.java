package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoTransacao {
    RECEITA(1L, "Receita"),
    DESPESA(2L, "Despesa");

    private final Long ID;
    private final String NOME;

    TipoTransacao(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static TipoTransacao valueOf(Long id) {
        if (id == null)
            return null;

        for (TipoTransacao t : TipoTransacao.values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}
