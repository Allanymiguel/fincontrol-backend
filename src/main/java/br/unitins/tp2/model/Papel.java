package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Papel implements EnumComId {
    USER(1L, "Usuário"),
    ADMIN(2L, "Administrador");

    private final Long ID;
    private final String NOME;

    Papel(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    @Override
    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static Papel valueOf(Long id) {
        if (id == null)
            return null;

        for (Papel p : Papel.values()) {
            if (Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }
}
