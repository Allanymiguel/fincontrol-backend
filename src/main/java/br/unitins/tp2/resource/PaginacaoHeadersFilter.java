package br.unitins.tp2.resource;

import br.unitins.tp2.dto.PageResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

// Toda listagem paginada devolve o PageResponse no corpo e repete a paginacao nos headers
// X-Page, X-Page-Size e X-Total-Count (expostos no CORS do application.properties).
// Fica num filtro so para valer em todas as listagens, atuais e novas, sem mexer em cada resource.
@Provider
public class PaginacaoHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!(response.getEntity() instanceof PageResponse<?> pagina))
            return;
        response.getHeaders().putSingle("X-Page", pagina.page());
        response.getHeaders().putSingle("X-Page-Size", pagina.pageSize());
        response.getHeaders().putSingle("X-Total-Count", pagina.totalItems());
    }
}
