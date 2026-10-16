package br.unitins.tp2.repository;

import br.unitins.tp2.model.Cupom;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CupomRepository implements PanacheRepository<Cupom> {

    public PanacheQuery<Cupom> findByCodigo(String codigo) {
        return find("lower(codigo) like lower(?1) order by validade desc, codigo", "%" + codigo + "%");
    }

    // usado no create/update: procura o codigo em OUTRO cupom que nao seja o que esta sendo editado
    public Cupom findByCodigoExceptId(String codigo, Long id) {
        if (id == null)
            return find("codigo", codigo).firstResult();
        return find("codigo = ?1 and id <> ?2", codigo, id).firstResult();
    }

    @Override
    public PanacheQuery<Cupom> findAll() {
        return find("order by validade desc, codigo");
    }
}
