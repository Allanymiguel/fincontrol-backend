package br.unitins.tp2.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.ConsultaCep;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Log so de insercao: a aplicacao so faz persist e consultas aqui (a entidade e @Immutable)
@ApplicationScoped
public class ConsultaCepRepository implements PanacheRepository<ConsultaCep> {

    // o ViaCEP falhou ou nao achou o CEP desde a data? (libera o endereco manual)
    public boolean existeFalhaOuNaoEncontradoDesde(String cep, LocalDateTime desde) {
        return count("cep = ?1 and fonte = ?2 and resultado in (?3, ?4) and dataHora >= ?5", cep,
                FonteConsultaCep.VIACEP, ResultadoConsultaCep.NAO_ENCONTRADO, ResultadoConsultaCep.FALHA, desde) > 0;
    }

    // filtros opcionais (nulo = nao filtra); [de, ate)
    public PanacheQuery<ConsultaCep> findByFiltros(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado,
            LocalDateTime de, LocalDateTime ate) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (cep != null && !cep.isBlank()) {
            condicoes.add("cep like :cep");
            params.put("cep", cep.replaceAll("\\D", "") + "%");
        }
        if (fonte != null) {
            condicoes.add("fonte = :fonte");
            params.put("fonte", fonte);
        }
        if (resultado != null) {
            condicoes.add("resultado = :resultado");
            params.put("resultado", resultado);
        }
        if (de != null) {
            condicoes.add("dataHora >= :de");
            params.put("de", de);
        }
        if (ate != null) {
            condicoes.add("dataHora < :ate");
            params.put("ate", ate);
        }
        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by dataHora desc, id desc", params);
    }
}
