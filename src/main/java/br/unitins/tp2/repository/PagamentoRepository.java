package br.unitins.tp2.repository;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.model.Pagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PagamentoRepository implements PanacheRepository<Pagamento> {

    // historico de cobrancas do usuario, da mais recente para a mais antiga
    public List<Pagamento> findByUsuario(Long idUsuario) {
        return list("assinatura.usuario.id = ?1 order by dataPagamento desc, id desc", idUsuario);
    }

    // pagamentos com data no intervalo (inclusive), com plano e assinatura (indicadores do painel)
    public List<Pagamento> findEntre(LocalDate de, LocalDate ate) {
        return list("from Pagamento p join fetch p.plano join fetch p.assinatura "
                + "where p.dataPagamento >= ?1 and p.dataPagamento <= ?2", de, ate);
    }

    // data do primeiro pagamento registrado; null se nao houver nenhum
    public LocalDate findPrimeiraData() {
        return getEntityManager().createQuery("select min(p.dataPagamento) from Pagamento p", LocalDate.class)
                .getSingleResult();
    }

    // usados para impedir a exclusao de plano/cupom que ja aparecem no historico
    public long countByPlano(Long idPlano) {
        return count("plano.id", idPlano);
    }

    public long countByCupom(Long idCupom) {
        return count("cupom.id", idCupom);
    }
}
