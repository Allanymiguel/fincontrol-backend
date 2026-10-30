package br.unitins.tp2.converter;

import br.unitins.tp2.model.SituacaoCep;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SituacaoCepConverter extends EnumIdConverter<SituacaoCep> {

    public SituacaoCepConverter() {
        super(SituacaoCep.class);
    }
}
