package br.unitins.tp2.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.model.FaixaIrpf;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FaixaIrpfRepository implements PanacheRepository<FaixaIrpf> {

    // faixas de uma tabela (mesmo inicio de vigencia), da menor para a maior
    public PanacheQuery<FaixaIrpf> findByVigencia(LocalDate vigenciaInicio) {
        return find("vigenciaInicio = ?1 order by limiteInferior", vigenciaInicio);
    }

    // datas de inicio das tabelas cadastradas, da mais recente para a mais antiga
    public List<LocalDate> findVigencias() {
        return getEntityManager()
                .createQuery("select distinct f.vigenciaInicio from FaixaIrpf f order by f.vigenciaInicio desc",
                        LocalDate.class)
                .getResultList();
    }

    // usado no create/update: procura outra faixa da mesma tabela comecando no mesmo valor
    public FaixaIrpf findByVigenciaAndLimiteInferiorExceptId(LocalDate vigenciaInicio, BigDecimal limiteInferior,
            Long id) {
        if (id == null)
            return find("vigenciaInicio = ?1 and limiteInferior = ?2", vigenciaInicio, limiteInferior).firstResult();
        return find("vigenciaInicio = ?1 and limiteInferior = ?2 and id <> ?3", vigenciaInicio, limiteInferior, id)
                .firstResult();
    }

    @Override
    public PanacheQuery<FaixaIrpf> findAll() {
        return find("order by vigenciaInicio desc, limiteInferior");
    }
}
