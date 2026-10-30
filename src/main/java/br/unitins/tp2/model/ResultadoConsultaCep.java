package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// O que a busca de CEP encontrou em cada fonte
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ResultadoConsultaCep implements EnumComId {
    ENCONTRADO(1L, "Encontrado"),
    NAO_ENCONTRADO(2L, "Não encontrado"),
    // timeout, sem internet ou servico fora do ar
    FALHA(3L, "Falha");

    private final Long ID;
    private final String NOME;

    ResultadoConsultaCep(Long id, String nome) {
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

    public static ResultadoConsultaCep valueOf(Long id) {
        if (id == null)
            return null;

        for (ResultadoConsultaCep r : ResultadoConsultaCep.values()) {
            if (Objects.equals(r.getId(), id))
                return r;
        }
        return null;
    }
}
