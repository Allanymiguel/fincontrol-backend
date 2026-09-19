package br.unitins.tp2.repository;

import br.unitins.tp2.model.Categoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaRepository implements PanacheRepository<Categoria> {

    public PanacheQuery<Categoria> findByNome(String nome) {
        return find("nome like ?1 order by nome", "%" + nome + "%");
    }

    @Override
    public PanacheQuery<Categoria> findAll() {
        return find("order by nome");
    }
}
