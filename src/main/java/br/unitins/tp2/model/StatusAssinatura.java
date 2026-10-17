package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusAssinatura {
    // aguardando pagamento (RN15: pagamento aprovado -> ATIVA). Reservado para um pagamento real:
    // com a cobranca simulada, toda assinatura fica ATIVA.
    PENDENTE(1L, "Pendente"),
    ATIVA(2L, "Ativa"),
    CANCELADA(3L, "Cancelada");

    private final Long ID;
    private final String NOME;

    StatusAssinatura(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static StatusAssinatura valueOf(Long id) {
        if (id == null)
            return null;

        for (StatusAssinatura s : StatusAssinatura.values()) {
            if (Objects.equals(s.getId(), id))
                return s;
        }
        return null;
    }
}
