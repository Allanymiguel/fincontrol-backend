package br.unitins.tp2.repository;

import br.unitins.tp2.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public Categoria findByNome(String nome) {
        return find("SELECT c FROM Categoria c WHERE c.nome = ?1 ", nome).firstResult();
    }

    public Categoria findByNomeExceptId(String nome, Long id) {
        if (id == null)
            return findByNome(nome);
        return find("SELECT c FROM Categoria c WHERE c.nome = ?1 AND c.id <> ?2 ", nome, id).firstResult();
    }

    public PanacheQuery<Categoria> findByNomeLike(String nome) {
        return find("SELECT c FROM Categoria c WHERE c.nome LIKE ?1 ", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Categoria> findAll() {
        return find("SELECT c FROM Categoria c ORDER BY c.nome");
    }

}
