package br.unitins.tp2.converter;

import br.unitins.tp2.model.TipoMudancaAssinatura;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMudancaAssinaturaConverter extends EnumIdConverter<TipoMudancaAssinatura> {

    public TipoMudancaAssinaturaConverter() {
        super(TipoMudancaAssinatura.class);
    }
}
