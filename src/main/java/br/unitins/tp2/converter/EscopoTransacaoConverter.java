package br.unitins.tp2.converter;

import br.unitins.tp2.model.EscopoTransacao;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EscopoTransacaoConverter extends EnumIdConverter<EscopoTransacao> {

    public EscopoTransacaoConverter() {
        super(EscopoTransacao.class);
    }
}
