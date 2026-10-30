package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.EventoAuditoriaResponseDTO;
import br.unitins.tp2.dto.FiltrosAuditoriaResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.EventoAuditoriaResponseMapper;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.EventoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.service.AuditoriaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Consulta da auditoria das acoes do admin. SOMENTE LEITURA: nao ha POST, PUT, PATCH nem DELETE;
// os eventos sao gravados pelos proprios services, na transacao de cada acao.
@RolesAllowed("ADMIN")
@Path("admin/auditoria")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Auditoria")
public class AuditoriaResource {

    @Inject
    AuditoriaService service;

    @GET
    @Operation(summary = "Eventos de auditoria, do mais recente para o mais antigo",
            description = "Filtros opcionais: idAdmin (sub do Keycloak), idAcao, idTipoAlvo, idAlvo, busca (parte da "
                    + "descrição do alvo) e período (inicio/fim, AAAA-MM-DD, inclusive). Documento e telefone "
                    + "nunca aparecem: só a indicação de que mudaram.")
    public PageResponse<EventoAuditoriaResponseDTO> buscarTodos(
            @QueryParam("idAdmin") String idAdmin,
            @QueryParam("idAcao") Long idAcao,
            @QueryParam("idTipoAlvo") Long idTipoAlvo,
            @QueryParam("idAlvo") Long idAlvo,
            @QueryParam("busca") String busca,
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        AuditoriaService.Filtro filtro = new AuditoriaService.Filtro(idAdmin, acao(idAcao), tipoAlvo(idTipoAlvo),
                idAlvo, busca, converterData(inicio, "inicio"), converterData(fim, "fim"));
        List<EventoAuditoria> eventos = service.buscar(filtro, page, pageSize);
        return PageResponse.of(eventos, page, pageSize, service.contar(filtro),
                EventoAuditoriaResponseMapper::toResponse);
    }

    // opcoes dos filtros: acoes, tipos de alvo e os admins que ja tem eventos
    @GET
    @Path("/filtros")
    public FiltrosAuditoriaResponseDTO buscarFiltros() {
        return service.filtros();
    }

    @GET
    @Path("/{id}")
    public EventoAuditoriaResponseDTO buscarPorId(@PathParam("id") Long id) {
        return EventoAuditoriaResponseMapper.toResponse(service.findById(id));
    }

    private AcaoAuditoria acao(Long idAcao) {
        AcaoAuditoria acao = AcaoAuditoria.valueOf(idAcao);
        if (idAcao != null && acao == null)
            throw ValidationException.of("idAcao", "Ação não encontrada.");
        return acao;
    }

    private TipoAlvoAuditoria tipoAlvo(Long idTipoAlvo) {
        TipoAlvoAuditoria tipo = TipoAlvoAuditoria.valueOf(idTipoAlvo);
        if (idTipoAlvo != null && tipo == null)
            throw ValidationException.of("idTipoAlvo", "Tipo de alvo não encontrado.");
        return tipo;
    }

    // a data vem como texto para uma data mal formada virar 400 com mensagem (e nao 404 vazio)
    private LocalDate converterData(String data, String campo) {
        if (data == null || data.isBlank())
            return null;
        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException e) {
            throw ValidationException.of(campo, "Data inválida. Use o formato AAAA-MM-DD.");
        }
    }
}
