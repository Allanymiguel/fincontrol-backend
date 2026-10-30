package br.unitins.tp2.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.EventoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

// Auditoria so de insercao: a aplicacao so faz persist e consultas aqui (a entidade e @Immutable)
@ApplicationScoped
public class EventoAuditoriaRepository implements PanacheRepository<EventoAuditoria> {

    // filtros opcionais (nulo = nao filtra); busca = parte da descricao do alvo; [de, ate)
    public PanacheQuery<EventoAuditoria> findByFiltros(String adminKeycloakId, AcaoAuditoria acao,
            TipoAlvoAuditoria tipoAlvo, Long idAlvo, String busca, LocalDateTime de, LocalDateTime ate) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (adminKeycloakId != null && !adminKeycloakId.isBlank()) {
            condicoes.add("adminKeycloakId = :admin");
            params.put("admin", adminKeycloakId);
        }
        if (acao != null) {
            condicoes.add("acao = :acao");
            params.put("acao", acao);
        }
        if (tipoAlvo != null) {
            condicoes.add("tipoAlvo = :tipoAlvo");
            params.put("tipoAlvo", tipoAlvo);
        }
        if (idAlvo != null) {
            condicoes.add("idAlvo = :idAlvo");
            params.put("idAlvo", idAlvo);
        }
        if (busca != null && !busca.isBlank()) {
            condicoes.add("lower(descricaoAlvo) like lower(:busca)");
            params.put("busca", "%" + busca.trim() + "%");
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

    // admins que ja tem eventos: [keycloakId, nome, email] (para o filtro da tela)
    public List<Object[]> findAdmins() {
        return getEntityManager()
                .createQuery("select e.adminKeycloakId, max(e.adminNome), max(e.adminEmail) from EventoAuditoria e "
                        + "where e.adminKeycloakId is not null group by e.adminKeycloakId order by max(e.adminNome)",
                        Object[].class)
                .getResultList();
    }
}
