package br.unitins.tp2.converter;

import br.unitins.tp2.model.Periodicidade;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PeriodicidadeConverter extends EnumIdConverter<Periodicidade> {

    public PeriodicidadeConverter() {
        super(Periodicidade.class);
    }
}
