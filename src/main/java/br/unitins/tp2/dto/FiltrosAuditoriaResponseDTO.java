package br.unitins.tp2.dto;

import java.util.List;

import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;

// Opcoes dos filtros da tela de auditoria
public record FiltrosAuditoriaResponseDTO(
    List<AcaoAuditoria> acoes,
    List<TipoAlvoAuditoria> tiposAlvo,
    // so os admins que ja tem eventos
    List<Admin> admins
) {

    public record Admin(String keycloakId, String nome, String email) {
    }
}
