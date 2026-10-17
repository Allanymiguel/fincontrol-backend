package br.unitins.tp2.repository;

import br.unitins.tp2.model.Plano;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PlanoRepository implements PanacheRepository<Plano> {

    public PanacheQuery<Plano> findByNome(String nome) {
        return find("lower(nome) like lower(?1) order by precoMensal, nome", "%" + nome + "%");
    }

    // usado no create/update: procura o mesmo nome (sem diferenciar maiusculas) em OUTRO plano
    // usada no cadastro (RN13) para localizar o plano Gratuito pelo nome
    public Plano findByNomeExato(String nome) {
        return find("lower(nome) = lower(?1)", nome).firstResult();
    }

    public Plano findByNomeExceptId(String nome, Long id) {
        if (id == null)
            return find("lower(nome) = lower(?1)", nome).firstResult();
        return find("lower(nome) = lower(?1) and id <> ?2", nome, id).firstResult();
    }

    @Override
    public PanacheQuery<Plano> findAll() {
        return find("order by precoMensal, nome");
    }
}
