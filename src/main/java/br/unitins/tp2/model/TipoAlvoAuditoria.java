package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// Sobre o que a acao foi feita. Na ASSINATURA o id do alvo e o do usuario (a assinatura e unica, RN16).
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TipoAlvoAuditoria implements EnumComId {
    USUARIO(1L, "Usuário"),
    ASSINATURA(2L, "Assinatura"),
    PLANO(3L, "Plano"),
    CUPOM(4L, "Cupom"),
    PARAMETRO_FISCAL(5L, "Parâmetro fiscal"),
    FAIXA_IRPF(6L, "Faixa do IRPF"),
    CEP(7L, "CEP"),
    CONFIGURACAO(8L, "Configuração");

    private final Long ID;
    private final String NOME;

    TipoAlvoAuditoria(Long id, String nome) {
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

    public static TipoAlvoAuditoria valueOf(Long id) {
        if (id == null)
            return null;

        for (TipoAlvoAuditoria t : TipoAlvoAuditoria.values()) {
            if (Objects.equals(t.getId(), id))
                return t;
        }
        return null;
    }
}
