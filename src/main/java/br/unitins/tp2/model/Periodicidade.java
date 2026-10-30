package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Periodicidade implements EnumComId {
    MENSAL(1L, "Mensal"),
    ANUAL(2L, "Anual");

    private final Long ID;
    private final String NOME;

    Periodicidade(Long id, String nome) {
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

    // data de fim de um ciclo que comeca em inicio (1 mes ou 1 ano depois)
    public LocalDate somarCiclo(LocalDate inicio) {
        return this == ANUAL ? inicio.plusYears(1) : inicio.plusMonths(1);
    }

    // equivalente mensal do valor de uma cobranca nesta periodicidade (anual / 12), em centavos
    public BigDecimal valorMensal(BigDecimal valorCobranca) {
        if (this == ANUAL)
            return valorCobranca.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return valorCobranca.setScale(2, RoundingMode.HALF_UP);
    }

    public static Periodicidade valueOf(Long id) {
        if (id == null)
            return null;

        for (Periodicidade p : Periodicidade.values()) {
            if (Objects.equals(p.getId(), id))
                return p;
        }
        return null;
    }
}
