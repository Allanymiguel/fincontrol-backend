package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.CepLocalResponseDTO;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.SituacaoCep;

public final class CepLocalResponseMapper {

    private CepLocalResponseMapper() {
    }

    public static CepLocalResponseDTO toResponse(CepLocal cep) {
        CepLocalResponseDTO.Divergencia divergencia = cep.getSituacao() != SituacaoCep.DIVERGENTE ? null
                : new CepLocalResponseDTO.Divergencia(cep.getLogradouroExterno(), cep.getBairroExterno(),
                        cep.getMunicipioExterno());
        return new CepLocalResponseDTO(
                cep.getId(),
                cep.getCep(),
                cep.getLogradouro(),
                cep.getBairro(),
                MunicipioResponseMapper.toResponse(cep.getMunicipio()),
                cep.getOrigem(),
                cep.getSituacao(),
                cep.getDataCadastro(),
                cep.getDataAlteracao(),
                cep.getDataRevalidacao(),
                divergencia);
    }
}
