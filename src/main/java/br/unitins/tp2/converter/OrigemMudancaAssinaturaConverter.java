package br.unitins.tp2.converter;

import br.unitins.tp2.model.OrigemMudancaAssinatura;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemMudancaAssinaturaConverter extends EnumIdConverter<OrigemMudancaAssinatura> {

    public OrigemMudancaAssinaturaConverter() {
        super(OrigemMudancaAssinatura.class);
    }
}
