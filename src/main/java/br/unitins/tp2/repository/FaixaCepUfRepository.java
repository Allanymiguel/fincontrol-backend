package br.unitins.tp2.repository;

import br.unitins.tp2.model.FaixaCepUf;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FaixaCepUfRepository implements PanacheRepository<FaixaCepUf> {

    // faixa que contem o CEP (8 digitos); null se o CEP nao pertence a nenhuma UF
    public FaixaCepUf findByCep(String cep) {
        return find("cepInicial <= ?1 and cepFinal >= ?1", cep).firstResult();
    }
}
