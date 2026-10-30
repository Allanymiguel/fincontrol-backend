package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.EventoAuditoriaResponseDTO;
import br.unitins.tp2.model.EventoAuditoria;
import br.unitins.tp2.util.AlteracoesAuditoria;

public final class EventoAuditoriaResponseMapper {

    private EventoAuditoriaResponseMapper() {
    }

    public static EventoAuditoriaResponseDTO toResponse(EventoAuditoria evento) {
        return new EventoAuditoriaResponseDTO(
                evento.getId(),
                evento.getDataHora(),
                evento.getAdminNome(),
                evento.getAdminEmail(),
                evento.getAcao(),
                evento.getTipoAlvo(),
                evento.getIdAlvo(),
                evento.getDescricaoAlvo(),
                AlteracoesAuditoria.deJson(evento.getAlteracoes()));
    }
}
