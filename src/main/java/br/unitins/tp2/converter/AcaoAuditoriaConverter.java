package br.unitins.tp2.converter;

import br.unitins.tp2.model.AcaoAuditoria;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AcaoAuditoriaConverter extends EnumIdConverter<AcaoAuditoria> {

    public AcaoAuditoriaConverter() {
        super(AcaoAuditoria.class);
    }
}
