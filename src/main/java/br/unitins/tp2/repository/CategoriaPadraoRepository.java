package br.unitins.tp2.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.CategoriaPadrao;
import br.unitins.tp2.model.TipoTransacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoriaPadraoRepository implements PanacheRepository<CategoriaPadrao> {

    // filtros opcionais: nome (parte do nome) e tipo; nulo = nao filtra.
    // As arquivadas ficam no fim da lista.
    public PanacheQuery<CategoriaPadrao> findByFiltros(String nome, TipoTransacao tipo) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (nome != null && !nome.isBlank()) {
            condicoes.add("lower(nome) like lower(:nome)");
            params.put("nome", "%" + nome.trim() + "%");
        }

        if (tipo != null) {
            condicoes.add("tipo = :tipo");
            params.put("tipo", tipo);
        }

        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by arquivada, tipo, nome", params);
    }

    // usado no create/update: procura o mesmo nome + tipo em OUTRA categoria padrao
    public CategoriaPadrao findByNomeAndTipoExceptId(String nome, TipoTransacao tipo, Long id) {
        if (id == null)
            return find("lower(nome) = lower(?1) and tipo = ?2", nome, tipo).firstResult();
        return find("lower(nome) = lower(?1) and tipo = ?2 and id <> ?3", nome, tipo, id).firstResult();
    }

    @Override
    public PanacheQuery<CategoriaPadrao> findAll() {
        return findByFiltros(null, null);
    }
}
