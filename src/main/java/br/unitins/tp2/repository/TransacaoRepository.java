package br.unitins.tp2.repository;

import br.unitins.tp2.model.Transacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    public PanacheQuery<Transacao> findByDescricao(String descricao) {
        return find("descricao like ?1 order by data desc, descricao", "%" + descricao + "%");
    }

    public PanacheQuery<Transacao> findByCategoria(Long idCategoria) {
        return find("categoria.id = ?1 order by data desc, descricao", idCategoria);
    }

    public PanacheQuery<Transacao> findByDescricaoAndCategoria(String descricao, Long idCategoria) {
        return find("descricao like ?1 and categoria.id = ?2 order by data desc, descricao",
                "%" + descricao + "%", idCategoria);
    }

    @Override
    public PanacheQuery<Transacao> findAll() {
        return find("order by data desc, descricao");
    }
}
