package br.unitins.tp2.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.SituacaoCep;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CepLocalRepository implements PanacheRepository<CepLocal> {

    public CepLocal findByCep(String cep) {
        return find("cep", cep).firstResult();
    }

    // filtros opcionais (nulo = nao filtra): inicio do CEP, origem, situacao e UF
    public PanacheQuery<CepLocal> findByFiltros(String cep, OrigemCep origem, SituacaoCep situacao, String uf) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (cep != null && !cep.isBlank()) {
            condicoes.add("cep like :cep");
            params.put("cep", cep.replaceAll("\\D", "") + "%");
        }
        if (origem != null) {
            condicoes.add("origem = :origem");
            params.put("origem", origem);
        }
        if (situacao != null) {
            condicoes.add("situacao = :situacao");
            params.put("situacao", situacao);
        }
        if (uf != null && !uf.isBlank()) {
            condicoes.add("municipio.estado.sigla = :uf");
            params.put("uf", uf.trim().toUpperCase());
        }
        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by cep", params);
    }

    // manuais ainda nao conferidos (revalidacao em lote)
    public List<CepLocal> findManuaisNaoVerificados() {
        return list("origem = ?1 and situacao = ?2 order by cep", OrigemCep.MANUAL, SituacaoCep.NAO_VERIFICADO);
    }

    // valores ja usados no municipio (autocompletar do modo manual): campo = "bairro" ou "logradouro"
    public List<String> findSugestoes(Long idMunicipio, String campo, String busca, int limite) {
        String coluna = "logradouro".equals(campo) ? "logradouro" : "bairro";
        return getEntityManager()
                .createQuery("select distinct c." + coluna + " from CepLocal c where c.municipio.id = :municipio and c."
                        + coluna + " is not null and lower(c." + coluna + ") like lower(:busca) order by c." + coluna,
                        String.class)
                .setParameter("municipio", idMunicipio)
                .setParameter("busca", "%" + (busca == null ? "" : busca.trim()) + "%")
                .setMaxResults(limite)
                .getResultList();
    }
}
