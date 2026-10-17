package br.unitins.tp2.converter;

import br.unitins.tp2.model.StatusAssinatura;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusAssinaturaConverter extends EnumIdConverter<StatusAssinatura> {

    public StatusAssinaturaConverter() {
        super(StatusAssinatura.class);
    }
}
