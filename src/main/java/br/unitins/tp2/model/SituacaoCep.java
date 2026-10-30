package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Conferencia do CEP da base local
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SituacaoCep implements EnumComId {
    VERIFICADO(1L, "Verificado"),
    NAO_VERIFICADO(2L, "Não verificado"),
    // a revalidacao com o ViaCEP achou diferenca
    DIVERGENTE(3L, "Divergente");

    private final Long ID;
    private final String NOME;

    SituacaoCep(Long id, String nome) {
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

    public static SituacaoCep valueOf(Long id) {
        if (id == null)
            return null;

        for (SituacaoCep s : SituacaoCep.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}
