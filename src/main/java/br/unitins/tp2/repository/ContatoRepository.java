package br.unitins.tp2.repository;

import br.unitins.tp2.model.Contato;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContatoRepository implements PanacheRepository<Contato> {

    public PanacheQuery<Contato> findByNome(String nome) {
        return find("lower(nome) like lower(?1) order by nome", "%" + nome + "%");
    }

    public Contato findByDocumento(String documento) {
        return find("documento", documento).firstResult();
    }

    // usado no update: procura o documento em OUTRO contato que nao seja o que esta sendo editado
    public Contato findByDocumentoExceptId(String documento, Long id) {
        if (id == null)
            return findByDocumento(documento);
        return find("documento = ?1 and id <> ?2", documento, id).firstResult();
    }

    @Override
    public PanacheQuery<Contato> findAll() {
        return find("order by nome");
    }
}
