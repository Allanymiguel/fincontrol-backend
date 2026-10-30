package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.CobrancaResponseDTO;
import br.unitins.tp2.dto.ExecucaoCobrancaResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.ExecucaoCobrancaResponseMapper;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.ExecucaoCobranca;
import br.unitins.tp2.model.OrigemExecucaoCobranca;
import br.unitins.tp2.model.StatusExecucaoCobranca;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.service.AuditoriaService;
import br.unitins.tp2.service.CobrancaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Cobranca das assinaturas: disparo manual (sem esperar o agendador das 03:00) e o historico das
// execucoes, para responder "a cobranca de ontem rodou direito?".
@RolesAllowed("ADMIN")
@Path("admin/cobrancas")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cobrança")
public class CobrancaResource {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    CobrancaService service;

    @Inject
    AuditoriaService auditoria;

    // data = ate quando cobrar (yyyy-MM-dd); sem data, vale hoje. Fica no historico e na auditoria.
    @POST
    @Path("/processar")
    @Operation(summary = "Roda a cobrança agora", description = "Cobra o que venceu até a data (padrão: hoje). "
            + "Cada assinatura é processada isolada: uma falha não desfaz as outras. A execução fica no histórico.")
    public CobrancaResponseDTO processar(@QueryParam("data") String data) {
        LocalDate referencia = converterData(data, "data");
        CobrancaResponseDTO resultado = service.executar(referencia == null ? LocalDate.now() : referencia,
                OrigemExecucaoCobranca.MANUAL, auditoria.adminLogado());

        Map<String, String> resumo = new LinkedHashMap<>();
        resumo.put("Situação", resultado.status().getNome());
        resumo.put("Assinaturas processadas", String.valueOf(resultado.assinaturasProcessadas()));
        resumo.put("Pagamentos gerados", String.valueOf(resultado.pagamentosGerados()));
        resumo.put("Valor total", resultado.valorTotal().toPlainString().replace('.', ','));
        auditoria.registrar(AcaoAuditoria.COBRANCA_MANUAL, TipoAlvoAuditoria.COBRANCA, resultado.idExecucao(),
                "Cobrança até " + resultado.data().format(DATA_BR), Map.of(), resumo);
        return resultado;
    }

    @GET
    @Path("/execucoes")
    @Operation(summary = "Histórico das execuções da cobrança (mais recentes primeiro)",
            description = "Filtros: período da data de referência (inicio/fim, AAAA-MM-DD), idOrigem e idStatus.")
    public PageResponse<ExecucaoCobrancaResponseDTO> execucoes(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim,
            @QueryParam("idOrigem") Long idOrigem,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        OrigemExecucaoCobranca origem = OrigemExecucaoCobranca.valueOf(idOrigem);
        if (idOrigem != null && origem == null)
            throw ValidationException.of("idOrigem", "Origem não encontrada.");
        StatusExecucaoCobranca status = StatusExecucaoCobranca.valueOf(idStatus);
        if (idStatus != null && status == null)
            throw ValidationException.of("idStatus", "Situação não encontrada.");
        LocalDate de = converterData(inicio, "inicio");
        LocalDate ate = converterData(fim, "fim");
        List<ExecucaoCobranca> execucoes = service.buscarExecucoes(de, ate, origem, status, page, pageSize);
        return PageResponse.of(execucoes, page, pageSize, service.contarExecucoes(de, ate, origem, status),
                ExecucaoCobrancaResponseMapper::toResponse);
    }

    @GET
    @Path("/execucoes/{id}")
    public ExecucaoCobrancaResponseDTO execucao(@PathParam("id") Long id) {
        return ExecucaoCobrancaResponseMapper.toResponse(service.findExecucao(id));
    }

    @GET
    @Path("/dias-sem-execucao")
    @Operation(summary = "Dias (até ontem) em que a cobrança automática não rodou")
    public List<LocalDate> diasSemExecucao(@QueryParam("inicio") String inicio, @QueryParam("fim") String fim) {
        LocalDate ate = converterData(fim, "fim");
        ate = ate == null ? LocalDate.now().minusDays(1) : ate;
        LocalDate de = converterData(inicio, "inicio");
        return service.diasSemAutomatica(de == null ? ate.minusDays(29) : de, ate);
    }

    @GET
    @Path("/resumo")
    @Operation(summary = "A cobrança de ontem rodou direito?",
            description = "A última execução automática de ontem (nula se não rodou), as falhas e os dias sem "
                    + "execução automática nos últimos N dias (padrão 30).")
    public ExecucaoCobrancaResponseDTO.Resumo resumo(@QueryParam("dias") @DefaultValue("30") int dias) {
        return service.resumo(Math.max(1, Math.min(dias, 365)));
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
