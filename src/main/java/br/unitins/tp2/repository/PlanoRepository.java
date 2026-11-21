package br.unitins.tp2.repository;

import br.unitins.tp2.model.Plano;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

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

    // trava a linha do plano ate o fim da transacao: dois envios de imagem ao mesmo tempo nao leem a
    // mesma chave antiga (cada um apaga so o objeto que ele mesmo substituiu)
    public Plano findByIdParaAlterar(long id) {
        return findById(id, LockModeType.PESSIMISTIC_WRITE);
    }

    // catalogo: os planos ativos e, mesmo inativo, o plano atual do usuario (ele continua vendo o que assina)
    public PanacheQuery<Plano> findCatalogo(Long idPlanoAtual) {
        if (idPlanoAtual == null)
            return find("ativo = true order by precoMensal, nome");
        return find("(ativo = true or id = ?1) order by precoMensal, nome", idPlanoAtual);
    }

    @Override
    public PanacheQuery<Plano> findAll() {
        return find("order by precoMensal, nome");
    }
}
