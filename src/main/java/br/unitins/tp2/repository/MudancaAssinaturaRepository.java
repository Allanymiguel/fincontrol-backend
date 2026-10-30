package br.unitins.tp2.repository;

import java.util.List;

import br.unitins.tp2.model.MudancaAssinatura;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Historico so de insercao: aqui nao ha update nem delete (a entidade e @Immutable)
@ApplicationScoped
public class MudancaAssinaturaRepository implements PanacheRepository<MudancaAssinatura> {

    // mudancas da assinatura do usuario, da mais antiga para a mais recente
    public List<MudancaAssinatura> findByUsuario(Long idUsuario) {
        return list("assinatura.usuario.id = ?1 order by data, id", idUsuario);
    }
}
