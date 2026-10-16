package br.unitins.tp2.mapper;

import java.time.LocalDate;

import br.unitins.tp2.dto.ParametroFiscalResponseDTO;
import br.unitins.tp2.model.ParametroFiscal;

public final class ParametroFiscalResponseMapper {

    private ParametroFiscalResponseMapper() {
    }

    public static ParametroFiscalResponseDTO toResponse(ParametroFiscal parametro) {
        return new ParametroFiscalResponseDTO(
                parametro.getId(),
                parametro.getChave(),
                parametro.getValor(),
                parametro.getVigenciaInicio(),
                parametro.getVigenciaFim(),
                parametro.isVigenteEm(LocalDate.now()));
    }
}
