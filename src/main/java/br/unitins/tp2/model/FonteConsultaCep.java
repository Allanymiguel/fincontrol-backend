package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Onde a busca de CEP do cadastro de cliente procurou
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FonteConsultaCep implements EnumComId {
    BASE_LOCAL(1L, "Base local"),
    VIACEP(2L, "ViaCEP");

    private final Long ID;
    private final String NOME;

    FonteConsultaCep(Long id, String nome) {
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

    public static FonteConsultaCep valueOf(Long id) {
        if (id == null)
            return null;

        for (FonteConsultaCep f : FonteConsultaCep.values()) {
            if (Objects.equals(f.getId(), id))
                return f;
        }
        return null;
    }
}
