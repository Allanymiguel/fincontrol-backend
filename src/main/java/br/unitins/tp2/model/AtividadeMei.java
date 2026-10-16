package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AtividadeMei {
    COMERCIO(1L, "Comércio"),
    SERVICO(2L, "Serviço"),
    AMBOS(3L, "Comércio e Serviço");

    private final Long ID;
    private final String NOME;

    AtividadeMei(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static AtividadeMei valueOf(Long id) {
        if (id == null)
            return null;

        for (AtividadeMei a : AtividadeMei.values()) {
            if (Objects.equals(a.getId(), id))
                return a;
        }
        return null;
    }
}
