package br.unitins.tp2.model;

import com.fasterxml.jackson.annotation.JsonFormat;

// O que acontece com a assinatura numa troca de plano (RN16, no estilo da Netflix)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EfeitoTroca {
    // plano mais caro (ou saindo do Gratuito): vale na hora
    IMEDIATO(1L, "Imediata"),
    // plano mais barato ou cancelamento: vale na proxima cobranca
    AGENDADO(2L, "Agendada"),
    // escolheu de novo o plano vigente: a troca que estava agendada e desfeita
    DESFAZ_AGENDAMENTO(3L, "Agendamento desfeito");

    private final Long ID;
    private final String NOME;

    EfeitoTroca(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }
}
