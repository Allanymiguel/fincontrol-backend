package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Quem disparou a execucao da cobranca
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemExecucaoCobranca implements EnumComId {
    // agendador diario (fincontrol.cobranca.cron)
    AUTOMATICA(1L, "Automática"),
    // botao "Rodar cobranca agora" do admin
    MANUAL(2L, "Manual");

    private final Long ID;
    private final String NOME;

    OrigemExecucaoCobranca(Long id, String nome) {
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

    public static OrigemExecucaoCobranca valueOf(Long id) {
        if (id == null)
            return null;

        for (OrigemExecucaoCobranca o : OrigemExecucaoCobranca.values()) {
            if (Objects.equals(o.getId(), id))
                return o;
        }
        return null;
    }
}
