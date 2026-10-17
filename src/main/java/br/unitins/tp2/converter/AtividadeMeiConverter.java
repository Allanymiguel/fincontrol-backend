package br.unitins.tp2.converter;

import br.unitins.tp2.model.AtividadeMei;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AtividadeMeiConverter extends EnumIdConverter<AtividadeMei> {

    public AtividadeMeiConverter() {
        super(AtividadeMei.class);
    }
}
