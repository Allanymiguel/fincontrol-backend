package br.unitins.tp2.converter;

import br.unitins.tp2.model.PerfilFiscal;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PerfilFiscalConverter extends EnumIdConverter<PerfilFiscal> {

    public PerfilFiscalConverter() {
        super(PerfilFiscal.class);
    }
}
