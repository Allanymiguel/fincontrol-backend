package br.unitins.tp2.converter;

import br.unitins.tp2.model.FormaPagamento;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FormaPagamentoConverter extends EnumIdConverter<FormaPagamento> {

    public FormaPagamentoConverter() {
        super(FormaPagamento.class);
    }
}
