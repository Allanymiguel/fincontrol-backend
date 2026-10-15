package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.ContatoResponseDTO;
import br.unitins.tp2.dto.EnderecoResponseDTO;
import br.unitins.tp2.model.Contato;
import br.unitins.tp2.model.Endereco;

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
                toResponse(contato.getEndereco()));
    }

    private static EnderecoResponseDTO toResponse(Endereco endereco) {
        if (endereco == null)
            return null;

        return new EnderecoResponseDTO(
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getComplemento(),
                endereco.getBairro(),
                endereco.getMunicipio() == null ? null
                        : MunicipioResponseMapper.toResponse(endereco.getMunicipio()));
    }
}
