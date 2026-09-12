package br.unitins.tp2.resource;

import java.util.List;

import br.unitins.tp2.dto.CategoriaDTO;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.service.CategoriaService;
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

@Path("categorias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriaResource {

    @Inject
    CategoriaService service;

    @GET
    public List<Categoria> buscarTodos(@QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findAll(page, pageSize);
    }

    @GET
    @Path("/nome/{nome}")
    public List<Categoria> buscarPorNome(@PathParam("nome") String nome, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("pageSize") @DefaultValue("100") int pageSize) {
        return service.findByNome(nome, page, pageSize);
    }

    @GET
    @Path("/{id}")
    public Categoria buscarPorId(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Categoria incluir(CategoriaDTO dto) {
        return service.create(dto);
    }

    @PUT
    @Path("/{id}")
    public void alterar(@PathParam("id") Long id, CategoriaDTO dto) {
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
    @Path("/nome/{nome}/count")
    public Long totalPorNome(@PathParam("nome") String nome) {
        return service.count(nome);
    }

}
