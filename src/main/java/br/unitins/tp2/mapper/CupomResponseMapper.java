package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.CupomResponseDTO;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.service.CupomService;

public final class CupomResponseMapper {

    private CupomResponseMapper() {
    }

    public static CupomResponseDTO toResponse(Cupom cupom, CupomService.Metricas metricas) {
        return new CupomResponseDTO(
                cupom.getId(),
                cupom.getCodigo(),
                cupom.getPercentualDesconto(),
                cupom.getValidade(),
                cupom.getAtivo(),
                cupom.getLimiteUsos(),
                cupom.getPlanos().stream()
                        .map(plano -> new CupomResponseDTO.PlanoDoCupom(plano.getId(), plano.getNome()))
                        .toList(),
                metricas.usos(),
                metricas.pagamentos(),
                metricas.descontoConcedido());
    }
}
