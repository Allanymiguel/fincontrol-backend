package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FormaPagamento implements EnumComId {
    PIX(1L, "Pix"),
    CARTAO(2L, "Cartão"),
    BOLETO(3L, "Boleto"),
    // usado nas assinaturas do plano gratuito, onde nao ha pagamento
    NENHUMA(4L, "Nenhuma");

    private final Long ID;
    private final String NOME;

    FormaPagamento(Long id, String nome) {
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

    public static FormaPagamento valueOf(Long id) {
        if (id == null)
            return null;

        for (FormaPagamento f : FormaPagamento.values()) {
            if (Objects.equals(f.getId(), id))
                return f;
        }
        return null;
    }
}
