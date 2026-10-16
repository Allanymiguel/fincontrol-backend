package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.EnderecoResponseDTO;
import br.unitins.tp2.model.Endereco;

// Usado por Contato e Usuario, que embutem o mesmo Endereco
public final class EnderecoResponseMapper {

    private EnderecoResponseMapper() {
    }

    public static EnderecoResponseDTO toResponse(Endereco endereco) {
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
