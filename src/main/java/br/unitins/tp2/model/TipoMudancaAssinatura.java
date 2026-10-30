package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoMudancaAssinatura implements EnumComId {
    // conta nova entrando no plano Gratuito (RN13)
    CADASTRO(1L, "Cadastro"),
    // troca que vale na hora para um plano de valor mensal maior
    UPGRADE(2L, "Upgrade"),
    // troca que vale na hora sem aumentar o valor mensal (ex.: entre planos sem custo)
    TROCA_IMEDIATA(3L, "Troca imediata"),
    DOWNGRADE_AGENDADO(4L, "Downgrade agendado"),
    // agendamento da volta ao Gratuito
    CANCELAMENTO_AGENDADO(5L, "Cancelamento agendado"),
    AGENDAMENTO_DESFEITO(6L, "Agendamento desfeito"),
    // a cobranca aplicou a troca agendada
    DOWNGRADE_APLICADO(7L, "Downgrade aplicado"),
    CANCELAMENTO_APLICADO(8L, "Cancelamento aplicado"),
    // a cobranca devolveu ao Gratuito uma conta desativada
    VOLTA_GRATUITO_CONTA_DESATIVADA(9L, "Volta ao Gratuito (conta desativada)");

    private final Long ID;
    private final String NOME;

    TipoMudancaAssinatura(Long id, String nome) {
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

    public static TipoMudancaAssinatura valueOf(Long id) {
        if (id == null)
            return null;

        for (TipoMudancaAssinatura t : TipoMudancaAssinatura.values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}
