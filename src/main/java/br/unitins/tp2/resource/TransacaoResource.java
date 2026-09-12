package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.TransacaoDTO;
import br.unitins.tp2.model.Transacao;
import br.unitins.tp2.service.TransacaoService;
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

@Path("transacoes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransacaoResource {

    @Inject
    TransacaoService service;

    @GET
    public List<Transacao> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize);
    }

    @GET
    @Path("/descricao/{descricao}")
    public List<Transacao> buscarPorDescricao(@PathParam("descricao") String descricao,
                                    @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByDescricao(descricao, page, pageSize);
    }

    @GET
    @Path("/categoria/{idCategoria}")
    public List<Transacao> buscarPorCategoria(@PathParam("idCategoria") Long idCategoria,
                                    @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByCategoria(idCategoria, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public Transacao buscarPorId(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Transacao incluir(TransacaoDTO dto) {
        return service.create(dto);
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, TransacaoDTO dto) {
        service.update(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void apagar(@PathParam("id") Long id) {
        service.delete(id);
    }

    @GET
    @Path("/count")
    public Long total() {
        return service.count();
    }

    @GET
    @Path("/descricao/{descricao}/count")
    public Long totalPorDescricao(@PathParam("descricao") String descricao) {
        return service.count(descricao);
    }

}
