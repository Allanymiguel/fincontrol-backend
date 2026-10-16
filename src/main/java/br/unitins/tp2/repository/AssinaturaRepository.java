package br.unitins.tp2.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.StatusAssinatura;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssinaturaRepository implements PanacheRepository<Assinatura> {

    // filtros opcionais: busca (parte do nome do usuario OU do plano) e status; nulo = nao filtra.
    // As mais recentes primeiro.
    public PanacheQuery<Assinatura> findByFiltros(String busca, StatusAssinatura status) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            condicoes.add("(lower(usuario.nome) like lower(:busca) or lower(plano.nome) like lower(:busca))");
            params.put("busca", "%" + busca.trim() + "%");
        }

        if (status != null) {
            condicoes.add("status = :status");
            params.put("status", status);
        }

        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by dataInicio desc, id desc", params);
    }

    // a assinatura ATIVA do usuario, ignorando a que esta sendo editada
    public Assinatura findAtivaDoUsuarioExceptId(Long idUsuario, Long id) {
        if (id == null)
            return find("usuario.id = ?1 and status = ?2", idUsuario, StatusAssinatura.ATIVA).firstResult();
        return find("usuario.id = ?1 and status = ?2 and id <> ?3", idUsuario, StatusAssinatura.ATIVA, id)
                .firstResult();
    }

    // usados para impedir a exclusao de plano/cupom que ja tem assinaturas
    public long countByPlano(Long idPlano) {
        return count("plano.id", idPlano);
    }

    public long countByCupom(Long idCupom) {
        return count("cupom.id", idCupom);
    }

    @Override
    public PanacheQuery<Assinatura> findAll() {
        return findByFiltros(null, null);
    }
}
