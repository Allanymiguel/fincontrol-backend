package br.unitins.tp2.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.ExecucaoCobranca;
import br.unitins.tp2.model.OrigemExecucaoCobranca;
import br.unitins.tp2.model.StatusExecucaoCobranca;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Historico so de insercao: a aplicacao so faz persist e consultas aqui (a entidade e @Immutable)
@ApplicationScoped
public class ExecucaoCobrancaRepository implements PanacheRepository<ExecucaoCobranca> {

    // filtros opcionais por data de referencia [de, ate], origem e status; mais recentes primeiro
    public PanacheQuery<ExecucaoCobranca> findByFiltros(LocalDate de, LocalDate ate, OrigemExecucaoCobranca origem,
            StatusExecucaoCobranca status) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (de != null) {
            condicoes.add("dataReferencia >= :de");
            params.put("de", de);
        }
        if (ate != null) {
            condicoes.add("dataReferencia <= :ate");
            params.put("ate", ate);
        }
        if (origem != null) {
            condicoes.add("origem = :origem");
            params.put("origem", origem);
        }
        if (status != null) {
            condicoes.add("status = :status");
            params.put("status", status);
        }
        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by inicio desc, id desc", params);
    }

    // dias (data de referencia) em que a automatica rodou
    public List<LocalDate> findDiasComAutomatica(LocalDate de, LocalDate ate) {
        return getEntityManager()
                .createQuery("select distinct e.dataReferencia from ExecucaoCobranca e where e.origem = :origem "
                        + "and e.dataReferencia >= :de and e.dataReferencia <= :ate", LocalDate.class)
                .setParameter("origem", OrigemExecucaoCobranca.AUTOMATICA)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .getResultList();
    }

    // a ultima execucao automatica do dia; null se nao rodou
    public ExecucaoCobranca findUltimaAutomatica(LocalDate dia) {
        return find("origem = ?1 and dataReferencia = ?2 order by inicio desc", OrigemExecucaoCobranca.AUTOMATICA, dia)
                .firstResult();
    }

    public long countFalhasDesde(LocalDate dia) {
        return count("status = ?1 and dataReferencia >= ?2", StatusExecucaoCobranca.FALHA, dia);
    }
}
