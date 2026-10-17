package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusPagamento implements EnumComId {
    PENDENTE(1L, "Pendente"),
    APROVADO(2L, "Aprovado"),
    RECUSADO(3L, "Recusado");

    private final Long ID;
    private final String NOME;

    StatusPagamento(Long id, String nome) {
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

    public static StatusPagamento valueOf(Long id) {
        if (id == null)
            return null;

        for (StatusPagamento s : StatusPagamento.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}
