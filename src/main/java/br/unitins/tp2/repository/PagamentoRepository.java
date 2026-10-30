package br.unitins.tp2.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.StatusPagamento;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PagamentoRepository implements PanacheRepository<Pagamento> {

    // quantidade e soma dos pagamentos de um status (resumo da tela de pagamentos)
    public record TotalPorStatus(StatusPagamento status, long quantidade, BigDecimal valor) {
    }

    private record Condicoes(String where, Map<String, Object> params) {
    }

    // historico de cobrancas do usuario, da mais recente para a mais antiga
    public List<Pagamento> findByUsuario(Long idUsuario) {
        return list("assinatura.usuario.id = ?1 order by dataPagamento desc, id desc", idUsuario);
    }

    // filtros opcionais: data do pagamento [de, ate], plano, forma, status e busca (parte do nome OU do
    // e-mail do usuario); mais recentes primeiro
    public PanacheQuery<Pagamento> findByFiltros(LocalDate de, LocalDate ate, Long idPlano, FormaPagamento forma,
            StatusPagamento status, String busca) {
        Condicoes c = condicoes(de, ate, idPlano, forma, status, busca);
        return find(c.where() + "order by dataPagamento desc, id desc", c.params());
    }

    // mesmos filtros, sem o de status: quantidade e soma por status
    public List<TotalPorStatus> totaisPorStatus(LocalDate de, LocalDate ate, Long idPlano, FormaPagamento forma,
            String busca) {
        Condicoes c = condicoes(de, ate, idPlano, forma, null, busca);
        var query = getEntityManager().createQuery("select status, count(*), sum(valor) from Pagamento "
                + (c.where().isEmpty() ? "" : "where " + c.where()) + "group by status", Object[].class);
        c.params().forEach(query::setParameter);
        return query.getResultStream()
                .map(linha -> new TotalPorStatus((StatusPagamento) linha[0], (Long) linha[1], (BigDecimal) linha[2]))
                .toList();
    }

    private static Condicoes condicoes(LocalDate de, LocalDate ate, Long idPlano, FormaPagamento forma,
            StatusPagamento status, String busca) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (de != null) {
            condicoes.add("dataPagamento >= :de");
            params.put("de", de);
        }
        if (ate != null) {
            condicoes.add("dataPagamento <= :ate");
            params.put("ate", ate);
        }
        if (idPlano != null) {
            condicoes.add("plano.id = :idPlano");
            params.put("idPlano", idPlano);
        }
        if (forma != null) {
            condicoes.add("formaPagamento = :forma");
            params.put("forma", forma);
        }
        if (status != null) {
            condicoes.add("status = :status");
            params.put("status", status);
        }
        if (busca != null && !busca.isBlank()) {
            condicoes.add("(lower(assinatura.usuario.nome) like lower(:busca) "
                    + "or lower(assinatura.usuario.email) like lower(:busca))");
            params.put("busca", "%" + busca.trim() + "%");
        }
        return new Condicoes(condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ", params);
    }

    // pagamentos com data no intervalo (inclusive), com plano e assinatura (indicadores do painel)
    public List<Pagamento> findEntre(LocalDate de, LocalDate ate) {
        return list("from Pagamento p join fetch p.plano join fetch p.assinatura "
                + "where p.dataPagamento >= ?1 and p.dataPagamento <= ?2", de, ate);
    }

    // pagamentos estornados com o estorno no intervalo (inclusive), seja qual for a data do pagamento
    public List<Pagamento> findEstornadosEntre(LocalDate de, LocalDate ate) {
        return list("status = ?1 and dataEstorno >= ?2 and dataEstorno < ?3", StatusPagamento.ESTORNADO,
                de.atStartOfDay(), ate.plusDays(1).atStartOfDay());
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
