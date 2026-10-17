package br.unitins.tp2.resource;

import br.unitins.tp2.dto.AssinaturaResponseDTO;
import br.unitins.tp2.dto.ObservacaoAssinaturaDTO;
import br.unitins.tp2.dto.TrocaPlanoDTO;
import br.unitins.tp2.dto.TrocaPlanoResponseDTO;
import br.unitins.tp2.mapper.AssinaturaResponseMapper;
import br.unitins.tp2.service.AssinaturaService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// A assinatura (unica) de um usuario, gerida pelo admin (RN16). Nao ha criacao nem exclusao:
// ela nasce no cadastro (RN13) e depois so troca de plano.
@RolesAllowed("ADMIN")
@Path("admin/usuarios/{idUsuario}/assinatura")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssinaturaAdminResource {

    @Inject
    AssinaturaService service;

    @GET
    public AssinaturaResponseDTO buscar(@PathParam("idUsuario") Long idUsuario) {
        return AssinaturaResponseMapper.toResponse(service.buscarDoUsuario(idUsuario));
    }

    // previa da troca (efeito, credito, proxima cobranca) sem alterar nada
    @POST
    @Path("/troca/simulacao")
    public TrocaPlanoResponseDTO simularTroca(@PathParam("idUsuario") Long idUsuario, TrocaPlanoDTO troca) {
        return service.simularTroca(idUsuario, troca);
    }

    @POST
    @Path("/troca")
    public TrocaPlanoResponseDTO trocarPlano(@PathParam("idUsuario") Long idUsuario, TrocaPlanoDTO troca) {
        return service.trocarPlano(idUsuario, troca);
    }

    // desfaz a troca agendada para a proxima cobranca
    @DELETE
    @Path("/troca")
    public AssinaturaResponseDTO desfazerTroca(@PathParam("idUsuario") Long idUsuario) {
        return AssinaturaResponseMapper.toResponse(service.desfazerTroca(idUsuario));
    }

    // cancelar o plano pago = voltar ao Gratuito na proxima cobranca
    @POST
    @Path("/cancelamento")
    public TrocaPlanoResponseDTO cancelarPlanoPago(@PathParam("idUsuario") Long idUsuario) {
        return service.cancelarPlanoPago(idUsuario);
    }

    @PATCH
    @Path("/observacao")
    public AssinaturaResponseDTO salvarObservacao(@PathParam("idUsuario") Long idUsuario,
            ObservacaoAssinaturaDTO observacao) {
        return AssinaturaResponseMapper.toResponse(service.salvarObservacao(idUsuario, observacao));
    }
}
