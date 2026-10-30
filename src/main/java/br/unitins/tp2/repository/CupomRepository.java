package br.unitins.tp2.repository;

import java.math.BigDecimal;
import java.util.List;

import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.StatusPagamento;
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

    // Adesoes (uso) = assinaturas com o cupom vigente, agendado ou ja pago com ele (estornado tambem:
    // a adesao aconteceu). O agendamento desfeito ou trocado antes de valer nao fica em lugar nenhum
    // e por isso libera o uso.
    private static final String ADESAO = "(a.cupom.id = :cupom or a.proximoCupom.id = :cupom or exists ("
            + "select p.id from Pagamento p where p.assinatura = a and p.cupom.id = :cupom and p.status in :pagos))";

    public long countAdesoes(Long idCupom) {
        return getEntityManager().createQuery("select count(a) from Assinatura a where " + ADESAO, Long.class)
                .setParameter("cupom", idCupom)
                .setParameter("pagos", List.of(StatusPagamento.APROVADO, StatusPagamento.ESTORNADO))
                .getSingleResult();
    }

    public boolean jaAderiu(Long idCupom, Long idAssinatura) {
        return getEntityManager().createQuery("select count(a) from Assinatura a where a.id = :assinatura and "
                + ADESAO, Long.class)
                .setParameter("assinatura", idAssinatura)
                .setParameter("cupom", idCupom)
                .setParameter("pagos", List.of(StatusPagamento.APROVADO, StatusPagamento.ESTORNADO))
                .getSingleResult() > 0;
    }

    // pagamentos aprovados com o cupom e o desconto concedido neles (estornados nao contam)
    public Object[] pagamentosEDesconto(Long idCupom) {
        return getEntityManager().createQuery("select count(p), coalesce(sum(p.valorDesconto), 0) from Pagamento p "
                + "where p.cupom.id = :cupom and p.status = :aprovado", Object[].class)
                .setParameter("cupom", idCupom)
                .setParameter("aprovado", StatusPagamento.APROVADO)
                .getSingleResult();
    }

    // cupons restritos ao plano (o plano nao pode ser excluido enquanto estiver neles)
    public long countByPlano(Long idPlano) {
        return getEntityManager().createQuery("select count(c) from Cupom c join c.planos p where p.id = :plano",
                Long.class).setParameter("plano", idPlano).getSingleResult();
    }

    public static BigDecimal comoValor(Object valor) {
        return valor instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(valor));
    }

    @Override
    public PanacheQuery<Cupom> findAll() {
        return find("order by validade desc, codigo");
    }
}
