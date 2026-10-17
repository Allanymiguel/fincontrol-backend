package br.unitins.tp2.converter;

import br.unitins.tp2.model.StatusPagamento;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatusPagamentoConverter extends EnumIdConverter<StatusPagamento> {

    public StatusPagamentoConverter() {
        super(StatusPagamento.class);
    }
}
