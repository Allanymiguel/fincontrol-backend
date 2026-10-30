package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// De onde veio o CEP da base local de enderecos de clientes
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OrigemCep implements EnumComId {
    API(1L, "API (ViaCEP)"),
    // digitado no cadastro do cliente quando o CEP nao foi encontrado
    MANUAL(2L, "Manual"),
    // carga por CSV feita pelo admin
    IMPORTACAO(3L, "Importação");

    private final Long ID;
    private final String NOME;

    OrigemCep(Long id, String nome) {
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

    public static OrigemCep valueOf(Long id) {
        if (id == null)
            return null;

        for (OrigemCep o : OrigemCep.values()) {
            if (Objects.equals(o.getId(), id))
                return o;
        }
        return null;
    }
}
