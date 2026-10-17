package br.unitins.tp2.repository;

import java.time.LocalDate;

import br.unitins.tp2.model.Transacao;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Todas as consultas sao escopadas ao usuario logado (RN12).
@ApplicationScoped
public class TransacaoRepository implements PanacheRepository<Transacao> {

    public PanacheQuery<Transacao> findAllByUsuario(Long idUsuario) {
        return find("usuario.id = ?1 order by data desc, descricao", idUsuario);
    }

    public PanacheQuery<Transacao> findByDescricaoAndUsuario(String descricao, Long idUsuario) {
        return find("usuario.id = ?1 and lower(descricao) like lower(?2) order by data desc, descricao",
                idUsuario, "%" + descricao + "%");
    }

    public PanacheQuery<Transacao> findByCategoriaAndUsuario(Long idCategoria, Long idUsuario) {
        return find("usuario.id = ?1 and categoria.id = ?2 order by data desc, descricao",
                idUsuario, idCategoria);
    }

    public PanacheQuery<Transacao> findByDescricaoAndCategoriaAndUsuario(String descricao, Long idCategoria,
            Long idUsuario) {
        return find(
                "usuario.id = ?1 and lower(descricao) like lower(?2) and categoria.id = ?3 "
                        + "order by data desc, descricao",
                idUsuario, "%" + descricao + "%", idCategoria);
    }

    // acesso cross-usuario devolve null (service traduz em 404)
    public Transacao findByIdAndUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and usuario.id = ?2", id, idUsuario).firstResult();
    }

    public long countByUsuario(Long idUsuario) {
        return count("usuario.id = ?1", idUsuario);
    }

    // data da transacao mais recente do usuario (resumo do admin); null se nao tiver nenhuma
    public LocalDate findUltimaDataByUsuario(Long idUsuario) {
        return getEntityManager()
                .createQuery("select max(t.data) from Transacao t where t.usuario.id = :idUsuario", LocalDate.class)
                .setParameter("idUsuario", idUsuario)
                .getSingleResult();
    }
}
