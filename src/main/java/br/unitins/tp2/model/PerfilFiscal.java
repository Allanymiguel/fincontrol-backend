package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PerfilFiscal implements EnumComId {
    PF(1L, "Pessoa Física"),
    MEI(2L, "Microempreendedor Individual"),
    ME(3L, "Microempresa");

    private final Long ID;
    private final String NOME;

    PerfilFiscal(Long id, String nome) {
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

    public static PerfilFiscal valueOf(Long id) {
        if (id == null)
            return null;

        for (PerfilFiscal p : PerfilFiscal.values()) {
            if (Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }
}
