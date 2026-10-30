package br.unitins.tp2.converter;

import br.unitins.tp2.model.OrigemCep;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemCepConverter extends EnumIdConverter<OrigemCep> {

    public OrigemCepConverter() {
        super(OrigemCep.class);
    }
}
