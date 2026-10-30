package br.unitins.tp2.converter;

import br.unitins.tp2.model.FonteConsultaCep;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FonteConsultaCepConverter extends EnumIdConverter<FonteConsultaCep> {

    public FonteConsultaCepConverter() {
        super(FonteConsultaCep.class);
    }
}
