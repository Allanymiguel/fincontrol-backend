package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Periodicidade {
    MENSAL(1L, "Mensal"),
    ANUAL(2L, "Anual");

    private final Long ID;
    private final String NOME;

    Periodicidade(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static Periodicidade valueOf(Long id) {
        if (id == null)
            return null;

        for (Periodicidade p : Periodicidade.values()) {
            if (Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }
}
