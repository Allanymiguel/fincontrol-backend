package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.FaixaIrpfResponseDTO;
import br.unitins.tp2.model.FaixaIrpf;

public final class FaixaIrpfResponseMapper {

    private FaixaIrpfResponseMapper() {
    }

    public static FaixaIrpfResponseDTO toResponse(FaixaIrpf faixa) {
        return new FaixaIrpfResponseDTO(
                faixa.getId(),
                faixa.getLimiteInferior(),
                faixa.getLimiteSuperior(),
                faixa.getAliquota(),
                faixa.getParcelaDeduzir(),
                faixa.getVigenciaInicio());
    }
}
