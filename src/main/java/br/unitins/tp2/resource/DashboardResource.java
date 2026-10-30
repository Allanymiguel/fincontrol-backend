package br.unitins.tp2.resource;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.DashboardResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.service.DashboardService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Painel inicial do admin: indicadores de assinatura e receita (RN12: so o administrador)
@RolesAllowed("ADMIN")
@Path("admin/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Dashboard administrativo")
public class DashboardResource {

    @Inject
    DashboardService service;

    @GET
    @Operation(summary = "Indicadores do painel do admin", description = """
            Só usa assinatura, plano e pagamento: nunca as finanças pessoais dos usuários.

            - **MRR** (hoje): soma, para cada assinatura ATIVA de plano pago (fora o Gratuito) de conta ativa, \
            do valor de cada cobrança já com o cupom, convertido para mensal (anual ÷ 12).
            - **Receita do mês**: pagamentos aprovados com data no mês (inclusive os estornados depois), \
            menos os estornos feitos no mês (de pagamentos de qualquer mês). Recusados não entram.
            - **Receita em risco** (hoje): perda de MRR dos downgrades agendados (MRR atual − MRR do plano \
            agendado) + MRR das assinaturas com cancelamento agendado + valor mensal das contas desativadas \
            com plano pago (fora do MRR: voltam ao Gratuito na próxima cobrança) + valor mensal das \
            assinaturas com pagamento recusado aguardando nova tentativa (fora do MRR: voltam ao Gratuito \
            na 3ª recusa seguida).
            - **Assinantes por plano** (hoje): contas ativas por plano vigente.
            - **Evolução**: reconstruída dos pagamentos. No último dia de cada mês (ou hoje, no mês atual), \
            uma assinatura é pagante do plano do seu pagamento aprovado mais recente se esse pagamento ainda \
            cobre o dia (1 mês ou 1 ano). Pagamento estornado continua cobrindo (o estorno só mexe na \
            receita); recusado não cobre. Contas cadastradas = contas USER com cadastro até o dia.
            - **Novos cadastros**: contas USER com data de cadastro no período.
            - **Conversão**: dos novos cadastros do período, percentual que já teve algum pagamento aprovado \
            (toda conta nasce no Gratuito, RN13). Nula quando não há cadastros.

            Meses anteriores ao primeiro dado do sistema vêm com `semHistorico = true` e valores nulos.""")
    public DashboardResponseDTO gerar(
            @Parameter(description = "Início do período (AAAA-MM-DD). Padrão: 1º dia de 5 meses atrás.")
            @QueryParam("inicio") String inicio,
            @Parameter(description = "Fim do período (AAAA-MM-DD), até hoje. Padrão: hoje.")
            @QueryParam("fim") String fim) {
        return service.gerar(converterData(inicio, "inicio"), converterData(fim, "fim"));
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
