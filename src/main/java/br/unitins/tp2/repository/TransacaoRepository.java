package br.unitins.tp2.repository;

import br.unitins.tp2.model.Transacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    public PanacheQuery<Transacao> findByDescricao(String descricao) {
        return find("SELECT t FROM Transacao t WHERE t.descricao LIKE ?1 ", "%" + descricao + "%");
    }

    public PanacheQuery<Transacao> findByCategoria(Long idCategoria) {
        return find("SELECT t FROM Transacao t WHERE t.categoria.id = ?1 ", idCategoria);
    }

    @Override
    public PanacheQuery<Transacao> findAll() {
        return find("SELECT t FROM Transacao t ORDER BY t.data DESC");
    }

}
