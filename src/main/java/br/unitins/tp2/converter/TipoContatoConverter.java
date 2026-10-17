package br.unitins.tp2.converter;

import br.unitins.tp2.model.TipoContato;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoContatoConverter extends EnumIdConverter<TipoContato> {

    public TipoContatoConverter() {
        super(TipoContato.class);
    }
}
