package br.unitins.tp2.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;

public record EventoAuditoriaResponseDTO(
    Long id,
    LocalDateTime dataHora,
    String adminNome,
    String adminEmail,
    AcaoAuditoria acao,
    TipoAlvoAuditoria tipoAlvo,
    Long idAlvo,
    String descricaoAlvo,
    // so os campos que mudaram (criacao: todos com "antes" vazio; exclusao: "depois" vazio)
    List<AlteracaoAuditoriaDTO> alteracoes
) {
}
