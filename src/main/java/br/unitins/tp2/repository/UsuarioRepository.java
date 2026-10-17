package br.unitins.tp2.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    // filtros opcionais: busca (parte do nome OU do email) e plano vigente da assinatura; nulo = nao filtra
    public PanacheQuery<Usuario> findByFiltros(String busca, Long idPlano) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            condicoes.add("(lower(nome) like lower(:busca) or lower(email) like lower(:busca))");
            params.put("busca", "%" + busca.trim() + "%");
        }

        if (idPlano != null) {
            condicoes.add("id in (select a.usuario.id from Assinatura a where a.plano.id = :idPlano)");
            params.put("idPlano", idPlano);
        }

        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by nome", params);
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
