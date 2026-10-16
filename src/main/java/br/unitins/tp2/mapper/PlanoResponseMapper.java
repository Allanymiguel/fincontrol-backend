package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.PlanoResponseDTO;
import br.unitins.tp2.model.Plano;

public final class PlanoResponseMapper {

    private PlanoResponseMapper() {
    }

    public static PlanoResponseDTO toResponse(Plano plano) {
        return new PlanoResponseDTO(
                plano.getId(),
                plano.getNome(),
                plano.getPrecoMensal(),
                plano.getPrecoAnual(),
                plano.getLimiteTransacoes(),
                plano.getLimiteMetas(),
                plano.getLiberaRecorrencias(),
                plano.getLiberaFiscal(),
                plano.getLiberaExportacao(),
                plano.getAtivo());
    }
}
