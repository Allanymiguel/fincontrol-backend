package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.EstornoPagamentoDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.PagamentoResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.PagamentoResponseMapper;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.service.PagamentoAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Pagamentos de todas as assinaturas (cobrancas simuladas, RN15) e o estorno. O usuario aparece so
// por nome e e-mail.
@RolesAllowed("ADMIN")
@Path("admin/pagamentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pagamentos")
public class PagamentoAdminResource {

    @Inject
    PagamentoAdminService service;

    @GET
    @Operation(summary = "Pagamentos (mais recentes primeiro)", description = "Filtros: período da data do "
            + "pagamento (inicio/fim, AAAA-MM-DD), idPlano, idFormaPagamento, idStatus e busca (parte do nome "
            + "ou do e-mail do usuário).")
    public PageResponse<PagamentoResponseDTO> buscar(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim,
            @QueryParam("idPlano") Long idPlano,
            @QueryParam("idFormaPagamento") Long idFormaPagamento,
            @QueryParam("idStatus") Long idStatus,
            @QueryParam("busca") String busca,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        PagamentoAdminService.Filtro filtro = filtro(inicio, fim, idPlano, idFormaPagamento, idStatus, busca);
        return PageResponse.of(service.buscar(filtro, page, pageSize), page, pageSize, service.contar(filtro),
                PagamentoResponseMapper::toResponse);
    }

    @GET
    @Path("/resumo")
    @Operation(summary = "Quantidade e soma por situação, com os mesmos filtros da lista (menos a situação)")
    public List<PagamentoResponseDTO.TotalPorStatus> resumo(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim,
            @QueryParam("idPlano") Long idPlano,
            @QueryParam("idFormaPagamento") Long idFormaPagamento,
            @QueryParam("busca") String busca) {
        return service.totais(filtro(inicio, fim, idPlano, idFormaPagamento, null, busca));
    }

    @GET
    @Path("/{id}")
    public PagamentoResponseDTO buscarPorId(@PathParam("id") Long id) {
        return PagamentoResponseMapper.toResponse(service.findById(id));
    }

    @POST
    @Path("/{id}/estorno")
    @Operation(summary = "Estorna um pagamento aprovado", description = "Motivo obrigatório. Só mexe no registro "
            + "financeiro (a receita do mês do estorno diminui): plano e período da assinatura continuam. "
            + "Pagamento recusado ou já estornado não pode ser estornado. Fica na auditoria.")
    public PagamentoResponseDTO estornar(@PathParam("id") Long id, EstornoPagamentoDTO estorno) {
        return PagamentoResponseMapper.toResponse(service.estornar(id, estorno));
    }

    private PagamentoAdminService.Filtro filtro(String inicio, String fim, Long idPlano, Long idFormaPagamento,
            Long idStatus, String busca) {
        FormaPagamento forma = FormaPagamento.valueOf(idFormaPagamento);
        if (idFormaPagamento != null && forma == null)
            throw ValidationException.of("idFormaPagamento", "Forma de pagamento não encontrada.");
        StatusPagamento status = StatusPagamento.valueOf(idStatus);
        if (idStatus != null && status == null)
            throw ValidationException.of("idStatus", "Situação não encontrada.");
        return new PagamentoAdminService.Filtro(converterData(inicio, "inicio"), converterData(fim, "fim"), idPlano,
                forma, status, busca);
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
