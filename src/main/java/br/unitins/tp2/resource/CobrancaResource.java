package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import br.unitins.tp2.dto.CobrancaResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.service.CobrancaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Roda a cobranca na hora, sem esperar o agendador diario (demonstracao e testes).
// data = ate quando cobrar (yyyy-MM-dd); sem data, vale hoje.
@RolesAllowed("ADMIN")
@Path("admin/cobrancas")
@Produces(MediaType.APPLICATION_JSON)
public class CobrancaResource {

    @Inject
    CobrancaService service;

    @POST
    @Path("/processar")
    public CobrancaResponseDTO processar(@QueryParam("data") String data) {
        return service.processar(converterData(data));
    }

    // a data vem como texto para uma data mal formada virar 400 com mensagem (e nao 404 vazio)
    private LocalDate converterData(String data) {
        if (data == null || data.isBlank())
            return LocalDate.now();

        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException e) {
            throw ValidationException.of("data", "Data inválida. Use o formato AAAA-MM-DD.");
        }
    }
}
