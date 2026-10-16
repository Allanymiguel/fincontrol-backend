package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import br.unitins.tp2.dto.FaixaIrpfRequestDTO;
import br.unitins.tp2.dto.FaixaIrpfResponseDTO;
import br.unitins.tp2.dto.PageResponse;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.FaixaIrpfResponseMapper;
import br.unitins.tp2.model.FaixaIrpf;
import br.unitins.tp2.service.FaixaIrpfService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// TODO: @RolesAllowed("ADMIN") - cadastro das faixas do IRPF e exclusivo do administrador (RN12)
@Path("faixas-irpf")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FaixaIrpfResource {

    @Inject
    FaixaIrpfService service;

    // vigencia (yyyy-MM-dd) = inicio de vigencia da tabela; sem ela, lista todas as faixas
    @GET
    public PageResponse<FaixaIrpfResponseDTO> buscarTodos(
            @QueryParam("vigencia") String vigenciaParam,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        LocalDate vigencia = converterVigencia(vigenciaParam);
        boolean hasVigencia = vigencia != null;

        List<FaixaIrpf> faixas = hasVigencia ? service.findByVigencia(vigencia, page, pageSize)
                : service.findAll(page, pageSize);
        long totalItems = hasVigencia ? service.count(vigencia) : service.count();

        return PageResponse.of(faixas, page, pageSize, totalItems, FaixaIrpfResponseMapper::toResponse);
    }

    // datas de inicio das tabelas cadastradas (para o filtro de vigencia do front)
    @GET
    @Path("/vigencias")
    public List<LocalDate> buscarVigencias() {
        return service.findVigencias();
    }

    @GET
    @Path("/{id}")
    public FaixaIrpfResponseDTO buscarPorId(@PathParam("id") Long id) {
        return FaixaIrpfResponseMapper.toResponse(service.findById(id));
    }

    @POST
    public FaixaIrpfResponseDTO incluir(FaixaIrpfRequestDTO dto) {
        return FaixaIrpfResponseMapper.toResponse(service.create(dto));
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, FaixaIrpfRequestDTO faixa) {
        service.update(id, faixa);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

    // recebe como texto para que uma data mal formatada vire 400 com Problem (e nao um 404 vazio do JAX-RS)
    private LocalDate converterVigencia(String vigencia) {
        if (vigencia == null || vigencia.isBlank())
            return null;

        try {
            return LocalDate.parse(vigencia.trim());
        } catch (DateTimeParseException e) {
            throw ValidationException.of("vigencia", "Informe a vigência no formato yyyy-MM-dd.");
        }
    }
}
