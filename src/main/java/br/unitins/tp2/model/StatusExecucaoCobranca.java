package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Como terminou a execucao da cobranca
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusExecucaoCobranca implements EnumComId {
    SUCESSO(1L, "Sucesso"),
    // algumas assinaturas falharam e as outras foram cobradas
    PARCIAL(2L, "Parcial"),
    // a execucao falhou ou nenhuma assinatura foi processada
    FALHA(3L, "Falha");

    private final Long ID;
    private final String NOME;

    StatusExecucaoCobranca(Long id, String nome) {
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

    public static StatusExecucaoCobranca valueOf(Long id) {
        if (id == null)
            return null;

        for (StatusExecucaoCobranca s : StatusExecucaoCobranca.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}
