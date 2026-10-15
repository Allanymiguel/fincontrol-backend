package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoContato {
    CLIENTE(1L, "Cliente"),
    FORNECEDOR(2L, "Fornecedor");

    private final Long ID;
    private final String NOME;

    TipoContato(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static TipoContato valueOf(Long id) {
        if (id == null)
            return null;

        for (TipoContato t : TipoContato.values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}
