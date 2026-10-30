package br.unitins.tp2.converter;

import br.unitins.tp2.model.OrigemExecucaoCobranca;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrigemExecucaoCobrancaConverter extends EnumIdConverter<OrigemExecucaoCobranca> {

    public OrigemExecucaoCobrancaConverter() {
        super(OrigemExecucaoCobranca.class);
    }
}
