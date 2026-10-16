package br.unitins.tp2.repository;

import java.time.LocalDate;

import br.unitins.tp2.model.ParametroFiscal;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParametroFiscalRepository implements PanacheRepository<ParametroFiscal> {

    public PanacheQuery<ParametroFiscal> findByChave(String chave) {
        return find("lower(chave) like lower(?1) order by chave, vigenciaInicio desc", "%" + chave + "%");
    }

    // usado no create/update: procura a mesma chave + inicio de vigencia em OUTRO parametro
    public ParametroFiscal findByChaveAndInicioExceptId(String chave, LocalDate vigenciaInicio, Long id) {
        if (id == null)
            return find("chave = ?1 and vigenciaInicio = ?2", chave, vigenciaInicio).firstResult();
        return find("chave = ?1 and vigenciaInicio = ?2 and id <> ?3", chave, vigenciaInicio, id).firstResult();
    }

    @Override
    public PanacheQuery<ParametroFiscal> findAll() {
        return find("order by chave, vigenciaInicio desc");
    }
}
