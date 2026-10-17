package br.unitins.tp2.dto;

import br.unitins.tp2.model.TipoContato;

public record ContatoResponseDTO(
    Long id,
    String nome,
    TipoContato tipo,
    String documento,
    String email,
    String telefone,
    EnderecoResponseDTO endereco,
    // false no cadastro simples (sem CPF/CNPJ e endereco)
    boolean cadastroCompleto
) {
}
