package br.unitins.tp2.model;

// Enum com codigo numerico explicito e estavel: e ele (e nao o nome nem a posicao da constante)
// que vai para o banco (converter/EnumIdConverter) e que a API recebe nos campos "idXxx".
public interface EnumComId {

    Long getId();
}
