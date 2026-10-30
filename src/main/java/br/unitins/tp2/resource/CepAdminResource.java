package br.unitins.tp2.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.tp2.dto.CepLocalRequestDTO;
import br.unitins.tp2.dto.CepLocalResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.dto.ResultadosCepDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.CepLocalResponseMapper;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.ConsultaCep;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import br.unitins.tp2.model.SituacaoCep;
import br.unitins.tp2.service.CepAdminService;
import br.unitins.tp2.util.RegrasCep;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Gestao da base de CEPs de clientes (so ADMIN). Cada acao gera evento de auditoria.
@RolesAllowed("ADMIN")
@Path("admin/ceps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Base de CEPs")
public class CepAdminResource {

    @Inject
    CepAdminService service;

    @GET
    @Operation(summary = "CEPs da base local", description = "Filtros: cep (início), idOrigem, idSituacao e uf.")
    public PageResponse<CepLocalResponseDTO> buscarTodos(
            @QueryParam("cep") String cep,
            @QueryParam("idOrigem") Long idOrigem,
            @QueryParam("idSituacao") Long idSituacao,
            @QueryParam("uf") String uf,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        OrigemCep origem = enumOuFalhar(OrigemCep.valueOf(idOrigem), idOrigem, "idOrigem", "Origem");
        SituacaoCep situacao = enumOuFalhar(SituacaoCep.valueOf(idSituacao), idSituacao, "idSituacao", "Situação");
        List<CepLocal> ceps = service.buscar(cep, origem, situacao, uf, page, pageSize);
        return PageResponse.of(ceps, page, pageSize, service.contar(cep, origem, situacao, uf),
                CepLocalResponseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public CepLocalResponseDTO buscarPorId(@PathParam("id") Long id) {
        return CepLocalResponseMapper.toResponse(service.findById(id));
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Corrige logradouro, bairro e município (da UF do CEP); verificar = marca como verificado")
    public CepLocalResponseDTO corrigir(@PathParam("id") Long id, CepLocalRequestDTO dto) {
        return CepLocalResponseMapper.toResponse(service.corrigir(id, dto));
    }

    @PATCH
    @Path("/{id}/verificacao")
    public CepLocalResponseDTO verificar(@PathParam("id") Long id) {
        return CepLocalResponseMapper.toResponse(service.verificar(id));
    }

    @POST
    @Path("/{id}/revalidacao")
    @Operation(summary = "Confere com o ViaCEP: bateu = verificado; não bateu = divergente (mostra a diferença)")
    public CepLocalResponseDTO revalidar(@PathParam("id") Long id) {
        return CepLocalResponseMapper.toResponse(service.revalidar(id));
    }

    @POST
    @Path("/revalidacao")
    @Operation(summary = "Revalida com o ViaCEP todos os CEPs manuais não verificados")
    public ResultadosCepDTO.RevalidacaoLote revalidarManuais() {
        return service.revalidarManuais();
    }

    @POST
    @Path("/importacao")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Importa CEPs de um CSV",
            description = "Arquivo UTF-8 (com ou sem BOM), separador \";\", cabeçalho cep;logradouro;bairro;codigo_ibge, "
                    + "até 1 MB ou 5.000 linhas. CEP verificado não é sobrescrito; os demais entram como Importação "
                    + "e não verificados. Devolve importados, atualizados e rejeitados (com linha e motivo).")
    public ResultadosCepDTO.Importacao importar(@RestForm("arquivo") FileUpload arquivo) {
        if (arquivo == null)
            throw ValidationException.of("arquivo", "Envie o arquivo CSV.");
        if (arquivo.size() > RegrasCep.MAX_BYTES_CSV)
            throw ValidationException.of("arquivo", "O arquivo pode ter no máximo 1 MB.");
        try {
            return service.importar(Files.readString(arquivo.uploadedFile(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            // inclui MalformedInputException: arquivo que nao esta em UTF-8
            throw ValidationException.of("arquivo", "Não foi possível ler o arquivo. Ele deve estar em UTF-8.");
        }
    }

    @GET
    @Path("/configuracao")
    public ResultadosCepDTO.Configuracao configuracao() {
        return service.configuracao();
    }

    @PUT
    @Path("/configuracao")
    @Operation(summary = "Liga ou desliga a consulta externa (ViaCEP) sem reiniciar o back")
    public ResultadosCepDTO.Configuracao definirConfiguracao(ResultadosCepDTO.Configuracao dto) {
        if (dto == null)
            throw ValidationException.of("apiExternaHabilitada", "Informe se a consulta externa fica ligada.");
        return service.definirApiExterna(dto.apiExternaHabilitada());
    }

    @GET
    @Path("/consultas")
    @Operation(summary = "Log das buscas de CEP do cadastro de clientes",
            description = "Filtros: cep (início), idFonte, idResultado e período (inicio/fim, AAAA-MM-DD).")
    public PageResponse<ResultadosCepDTO.Consulta> consultas(
            @QueryParam("cep") String cep,
            @QueryParam("idFonte") Long idFonte,
            @QueryParam("idResultado") Long idResultado,
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {
        FonteConsultaCep fonte = enumOuFalhar(FonteConsultaCep.valueOf(idFonte), idFonte, "idFonte", "Fonte");
        ResultadoConsultaCep resultado = enumOuFalhar(ResultadoConsultaCep.valueOf(idResultado), idResultado,
                "idResultado", "Resultado");
        LocalDate de = converterData(inicio, "inicio");
        LocalDate ate = converterData(fim, "fim");
        List<ConsultaCep> consultas = service.consultas(cep, fonte, resultado, de, ate, page, pageSize);
        return PageResponse.of(consultas, page, pageSize, service.contarConsultas(cep, fonte, resultado, de, ate),
                c -> new ResultadosCepDTO.Consulta(c.getId(), c.getCep(), c.getDataHora(), c.getFonte(),
                        c.getResultado()));
    }

    private static <E> E enumOuFalhar(E valor, Long id, String campo, String rotulo) {
        if (id != null && valor == null)
            throw ValidationException.of(campo, rotulo + " não encontrada.");
        return valor;
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
