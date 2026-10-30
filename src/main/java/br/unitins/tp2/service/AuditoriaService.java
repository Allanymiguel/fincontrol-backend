package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.FiltrosAuditoriaResponseDTO;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.EventoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;

// Registro (so de insercao) e consulta das acoes do administrador
public interface AuditoriaService {

    // filtros opcionais da consulta (nulo = nao filtra)
    record Filtro(String idAdmin, AcaoAuditoria acao, TipoAlvoAuditoria tipoAlvo, Long idAlvo, String busca,
            LocalDate inicio, LocalDate fim) {
    }

    // Registra a acao na transacao de quem chama, com o admin do token e so os campos que mudaram
    // entre os retratos (RetratoAuditoria). Criacao: antes vazio; exclusao: depois vazio.
    // Edicao que nao mudou nada nao vira evento.
    void registrar(AcaoAuditoria acao, TipoAlvoAuditoria tipoAlvo, Long idAlvo, String descricaoAlvo,
            Map<String, String> antes, Map<String, String> depois);

    // a requisicao atual e de um admin autenticado (o cadastro publico nao e auditado)
    boolean isAdminLogado();

    // admin dono do token (keycloakId nulo no modo dev sem login)
    record Admin(String keycloakId, String nome, String email) {
    }

    Admin adminLogado();

    List<EventoAuditoria> buscar(Filtro filtro, int page, int pageSize);

    long contar(Filtro filtro);

    EventoAuditoria findById(long id);

    FiltrosAuditoriaResponseDTO filtros();
}
