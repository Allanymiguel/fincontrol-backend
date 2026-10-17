package br.unitins.tp2.converter;

import br.unitins.tp2.model.EnumComId;
import jakarta.persistence.AttributeConverter;

// Grava o enum pelo seu codigo explicito (getId) e le de volta procurando a constante por esse
// codigo. Nunca usa o nome nem o ordinal: reordenar ou renomear as constantes nao muda o banco.
// Cada enum persistido tem uma subclasse de uma linha com @Converter(autoApply = true).
public abstract class EnumIdConverter<E extends Enum<E> & EnumComId> implements AttributeConverter<E, Integer> {

    private final Class<E> tipo;

    protected EnumIdConverter(Class<E> tipo) {
        this.tipo = tipo;
    }

    @Override
    public Integer convertToDatabaseColumn(E valor) {
        return valor == null ? null : valor.getId().intValue();
    }

    @Override
    public E convertToEntityAttribute(Integer codigo) {
        if (codigo == null)
            return null;

        for (E constante : tipo.getEnumConstants()) {
            if (constante.getId().intValue() == codigo)
                return constante;
        }
        // so acontece com dado corrompido no banco: os codigos validos sao os dos enums
        throw new IllegalStateException("Código " + codigo + " inválido para " + tipo.getSimpleName() + ".");
    }
}
