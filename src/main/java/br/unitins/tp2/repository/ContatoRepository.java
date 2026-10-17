package br.unitins.tp2.repository;

import br.unitins.tp2.model.Contato;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Todas as consultas sao escopadas ao usuario logado (RN12). O documento (CPF/CNPJ) e unico
// por usuario, nao globalmente: dois usuarios podem ter o mesmo contato (mesmo fornecedor).
@ApplicationScoped
public class ContatoRepository implements PanacheRepository<Contato> {

    public PanacheQuery<Contato> findAllByUsuario(Long idUsuario) {
        return find("usuario.id = ?1 order by nome", idUsuario);
    }

    public PanacheQuery<Contato> findByNomeAndUsuario(String nome, Long idUsuario) {
        return find("usuario.id = ?1 and lower(nome) like lower(?2) order by nome",
                idUsuario, "%" + nome + "%");
    }

    // acesso cross-usuario devolve null (service traduz em 404)
    public Contato findByIdAndUsuario(Long id, Long idUsuario) {
        return find("id = ?1 and usuario.id = ?2", id, idUsuario).firstResult();
    }

    // usado no create/update: procura o mesmo documento em OUTRO contato do MESMO usuario
    public Contato findByDocumentoExceptIdAndUsuario(String documento, Long idContato, Long idUsuario) {
        if (idContato == null)
            return find("documento = ?1 and usuario.id = ?2", documento, idUsuario).firstResult();
        return find("documento = ?1 and id <> ?2 and usuario.id = ?3", documento, idContato, idUsuario)
                .firstResult();
    }

    public long countByUsuario(Long idUsuario) {
        return count("usuario.id = ?1", idUsuario);
    }
}
