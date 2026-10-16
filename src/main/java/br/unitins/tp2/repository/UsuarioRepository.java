package br.unitins.tp2.repository;

import br.unitins.tp2.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    // o mesmo texto procura no nome ou no email
    public PanacheQuery<Usuario> findByNomeOuEmail(String busca) {
        return find("lower(nome) like lower(?1) or lower(email) like lower(?1) order by nome", "%" + busca + "%");
    }

    public Usuario findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResult();
    }

    public Usuario findByEmail(String email) {
        return find("lower(email) = lower(?1)", email).firstResult();
    }

    @Override
    public PanacheQuery<Usuario> findAll() {
        return find("order by nome");
    }
}
