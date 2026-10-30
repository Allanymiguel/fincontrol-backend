package br.unitins.tp2.converter;

import br.unitins.tp2.model.StatusExecucaoCobranca;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusExecucaoCobrancaConverter extends EnumIdConverter<StatusExecucaoCobranca> {

    public StatusExecucaoCobrancaConverter() {
        super(StatusExecucaoCobranca.class);
    }
}
