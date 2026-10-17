package br.unitins.tp2.converter;

import br.unitins.tp2.model.Papel;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PapelConverter extends EnumIdConverter<Papel> {

    public PapelConverter() {
        super(Papel.class);
    }
}
