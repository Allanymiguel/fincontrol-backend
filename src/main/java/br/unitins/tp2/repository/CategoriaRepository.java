package br.unitins.tp2.repository;

import br.unitins.tp2.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Todas as consultas sao escopadas ao usuario logado (RN12).
@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public PanacheQuery<Categoria> findAllByUsuario(Long idUsuario) {
        return find("usuario.id = ?1 order by nome", idUsuario);
    }

    public PanacheQuery<Categoria> findByNomeAndUsuario(String nome, Long idUsuario) {
        return find("usuario.id = ?1 and lower(nome) like lower(?2) order by nome",
                idUsuario, "%" + nome + "%");
    }

    // acesso cross-usuario devolve null (service traduz em 404, sem vazar existencia)
    public Categoria findByIdAndUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and usuario.id = ?2", id, idUsuario).firstResult();
    }

    public long countByUsuario(Long idUsuario) {
        return count("usuario.id = ?1", idUsuario);
    }
}
