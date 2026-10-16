package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.CupomResponseDTO;
import br.unitins.tp2.model.Cupom;

public final class CupomResponseMapper {

    private CupomResponseMapper() {
    }

    public static CupomResponseDTO toResponse(Cupom cupom) {
        return new CupomResponseDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getPercentualDesconto(),
                cupom.getValidade(),
                cupom.getAtivo());
    }
}
