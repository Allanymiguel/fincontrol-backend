package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.ContatoResponseDTO;
import br.unitins.tp2.model.Contato;

public final class ContatoResponseMapper {

    private ContatoResponseMapper() {
    }

    public static ContatoResponseDTO toResponse(Contato contato) {
        return new ContatoResponseDTO(
                contato.getId(),
                contato.getNome(),
                contato.getTipo(),
                contato.getDocumento(),
                contato.getEmail(),
                contato.getTelefone(),
                EnderecoResponseMapper.toResponse(contato.getEndereco()));
    }
}
