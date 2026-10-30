package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemMudancaAssinatura implements EnumComId {
    CADASTRO(1L, "Cadastro"),
    ADMIN(2L, "Administrador"),
    COBRANCA(3L, "Cobrança");

    private final Long ID;
    private final String NOME;

    OrigemMudancaAssinatura(Long id, String nome) {
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

    public static OrigemMudancaAssinatura valueOf(Long id) {
        if (id == null)
            return null;

        for (OrigemMudancaAssinatura o : OrigemMudancaAssinatura.values()) {
            if (Objects.equals(o.getId(), id))
                return o;
        }
        return null;
    }
}
