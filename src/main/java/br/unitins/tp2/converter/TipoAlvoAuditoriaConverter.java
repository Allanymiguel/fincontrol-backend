package br.unitins.tp2.converter;

import br.unitins.tp2.model.TipoAlvoAuditoria;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoAlvoAuditoriaConverter extends EnumIdConverter<TipoAlvoAuditoria> {

    public TipoAlvoAuditoriaConverter() {
        super(TipoAlvoAuditoria.class);
    }
}
