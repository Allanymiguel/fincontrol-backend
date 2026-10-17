package br.unitins.tp2.repository;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.model.Assinatura;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssinaturaRepository implements PanacheRepository<Assinatura> {

    // cada usuario tem no maximo uma (RN16)
    public Assinatura findByUsuario(Long idUsuario) {
        return find("usuario.id", idUsuario).firstResult();
    }

    // assinaturas de uma pagina da listagem de usuarios, numa consulta so
    public List<Assinatura> findByUsuarios(List<Long> idsUsuarios) {
        if (idsUsuarios.isEmpty())
            return List.of();
        return list("usuario.id in ?1", idsUsuarios);
    }

    // com cobranca vencida ate a data (inclusive): o agendador processa estas
    public List<Assinatura> findComCobrancaAte(LocalDate data) {
        return list("dataProximaCobranca <= ?1 order by dataProximaCobranca, id", data);
    }

    // usados para impedir a exclusao de plano/cupom em uso (vigente ou agendado)
    public long countByPlano(Long idPlano) {
        return count("plano.id = ?1 or proximoPlano.id = ?1", idPlano);
    }

    public long countByCupom(Long idCupom) {
        return count("cupom.id = ?1 or proximoCupom.id = ?1", idCupom);
    }
}
