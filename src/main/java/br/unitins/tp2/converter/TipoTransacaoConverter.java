package br.unitins.tp2.converter;

import br.unitins.tp2.model.TipoTransacao;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoTransacaoConverter extends EnumIdConverter<TipoTransacao> {

    public TipoTransacaoConverter() {
        super(TipoTransacao.class);
    }
}
