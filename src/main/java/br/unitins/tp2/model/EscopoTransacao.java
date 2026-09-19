package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EscopoTransacao {
    PESSOAL(1L, "Pessoal"),
    EMPRESA(2L, "Empresa");

    private final Long ID;
    private final String NOME;

    EscopoTransacao(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static EscopoTransacao valueOf(Long id) {
        if (id == null)
            return null;

        for (EscopoTransacao e : EscopoTransacao.values()) {
            if (Objects.equals(e.getId(), id))
                return e;
        }
        return null;
    }
}
