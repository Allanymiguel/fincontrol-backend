package br.unitins.tp2.converter;

import br.unitins.tp2.model.ResultadoConsultaCep;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ResultadoConsultaCepConverter extends EnumIdConverter<ResultadoConsultaCep> {

    public ResultadoConsultaCepConverter() {
        super(ResultadoConsultaCep.class);
    }
}
