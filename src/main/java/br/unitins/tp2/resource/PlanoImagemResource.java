package br.unitins.tp2.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import br.unitins.tp2.dto.PlanoResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.PlanoResponseMapper;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.service.PlanoService;
import br.unitins.tp2.util.RegrasImagem;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

// Imagem do plano no catalogo (uma por plano, guardada no S3). Ler e publico: o <img> do front nao
// leva token. Enviar, trocar e remover sao so do ADMIN.
@RolesAllowed("ADMIN")
@Path("planos/{id}/imagem")
@Tag(name = "Planos")
public class PlanoImagemResource {

    @Inject
    PlanoService service;

    // Sem @Produces: o tipo da resposta e o da imagem (e os erros saem como problem+json pelos mappers).
    // O 304 sai so com o banco (a versao esta nos metadados), sem chamar o S3.
    @GET
    @PermitAll
    @Operation(summary = "Imagem do plano (pública)", description = "Com ?v= da versão atual, a resposta "
            + "fica em cache por 1 ano (immutable); sem ela, o navegador revalida com If-None-Match (304). "
            + "download=true responde como anexo.")
    public Response buscar(@PathParam("id") long id, @QueryParam("v") String versaoPedida,
            @QueryParam("download") @DefaultValue("false") boolean download, @Context Request request) {
        ArquivoArmazenado imagem = service.imagemDoPlano(id);
        String versao = RegrasImagem.versao(imagem.getChave());
        String etag = RegrasImagem.etag(versao);
        String cache = RegrasImagem.cacheControl(versaoPedida, versao);

        Response.ResponseBuilder naoModificada = request.evaluatePreconditions(new EntityTag(versao));
        if (naoModificada != null)
            return naoModificada.header(HttpHeaders.ETAG, etag).header(HttpHeaders.CACHE_CONTROL, cache).build();

        return Response.ok(service.conteudoDaImagem(imagem))
                .type(imagem.getTipo())
                .header(HttpHeaders.ETAG, etag)
                .header(HttpHeaders.CACHE_CONTROL, cache)
                .header("Content-Disposition", RegrasImagem.contentDisposition(imagem.getNome(), download))
                // o navegador nao "adivinha" outro tipo, e a resposta aberta direto nao executa nada
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .build();
    }

    // multipart com a parte "arquivo". O tipo vem do conteudo (magic bytes), nao do nome nem do Content-Type.
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Envia ou troca a imagem do plano", description = "Parte \"arquivo\": JPEG, PNG ou "
            + "WebP de até 2 MB, conferido pelo conteúdo. A imagem anterior é apagada do armazenamento depois "
            + "que a nova é gravada. Corpo acima de 3 MB recebe 413.")
    public PlanoResponseDTO enviar(@PathParam("id") long id, @RestForm(RegrasImagem.CAMPO) FileUpload arquivo) {
        if (arquivo == null)
            throw ValidationException.of(RegrasImagem.CAMPO, "Envie a imagem do plano.");
        return PlanoResponseMapper.toResponse(
                service.enviarImagem(id, arquivo.fileName(), arquivo.size(), arquivo.uploadedFile()));
    }

    @DELETE
    @Operation(summary = "Remove a imagem do plano", description = "Sem imagem, não faz nada (204).")
    public void remover(@PathParam("id") long id) {
        service.removerImagem(id);
    }
}
