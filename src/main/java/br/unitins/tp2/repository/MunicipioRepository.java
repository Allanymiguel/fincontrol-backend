package br.unitins.tp2.repository;

import java.util.List;

import br.unitins.tp2.model.Municipio;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MunicipioRepository implements PanacheRepository<Municipio> {

    public Municipio findByCodigoIbge(String codigoIbge) {
        return find("codigoIbge", codigoIbge).firstResult();
    }

    // municipios da UF cujo nome contem a busca (selecao do endereco manual), em ordem alfabetica
    public List<Municipio> findByUf(String uf, String busca, int limite) {
        return find("estado.sigla = ?1 and lower(nome) like lower(?2) order by nome", uf.trim().toUpperCase(),
                "%" + (busca == null ? "" : busca.trim()) + "%").page(0, limite).list();
    }
}
