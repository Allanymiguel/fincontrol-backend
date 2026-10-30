package br.unitins.tp2.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.model.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    // filtros opcionais: busca (parte do nome OU do email) e plano vigente da assinatura; nulo = nao filtra
    // ativo: true/false/nulo; incompleto = cliente sem documento, perfil fiscal ou endereco;
    // agendamento = assinatura com troca ou cancelamento agendado
    public PanacheQuery<Usuario> findByFiltros(String busca, Long idPlano, Boolean ativo, boolean incompleto,
            boolean agendamento) {
        List<String> condicoes = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (busca != null && !busca.isBlank()) {
            condicoes.add("(lower(nome) like lower(:busca) or lower(email) like lower(:busca))");
            params.put("busca", "%" + busca.trim() + "%");
        }

        if (idPlano != null) {
            condicoes.add("id in (select a.usuario.id from Assinatura a where a.plano.id = :idPlano)");
            params.put("idPlano", idPlano);
        }

        if (ativo != null) {
            condicoes.add("ativo = :ativo");
            params.put("ativo", ativo);
        }

        if (incompleto) {
            condicoes.add("papel = :papelCliente and (documento is null or perfilFiscal is null or endereco.municipio is null)");
            params.put("papelCliente", Papel.USER);
        }

        if (agendamento) {
            condicoes.add("id in (select a.usuario.id from Assinatura a where a.proximoPlano is not null)");
        }

        String where = condicoes.isEmpty() ? "" : String.join(" and ", condicoes) + " ";
        return find(where + "order by nome", params);
    }

    // dia do cadastro de cada conta com o papel (indicadores do painel)
    public List<LocalDate> findDatasCadastro(Papel papel) {
        return getEntityManager()
                .createQuery("select u.dataCadastro from Usuario u where u.papel = :papel and u.dataCadastro is not null",
                        LocalDateTime.class)
                .setParameter("papel", papel)
                .getResultStream()
                .map(LocalDateTime::toLocalDate)
                .toList();
    }

    // contas com o papel cadastradas no intervalo [de, ate) que ja tiveram algum pagamento aprovado
    // (estornado depois tambem conta: o estorno so mexe na receita)
    public long countCadastradosComPagamentoAprovado(Papel papel, LocalDateTime de, LocalDateTime ate) {
        return getEntityManager()
                .createQuery("select count(u) from Usuario u where u.papel = :papel "
                        + "and u.dataCadastro >= :de and u.dataCadastro < :ate "
                        + "and exists (select p.id from Pagamento p where p.assinatura.usuario = u and p.status in :pagos)",
                        Long.class)
                .setParameter("papel", papel)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .setParameter("pagos", List.of(StatusPagamento.APROVADO, StatusPagamento.ESTORNADO))
                .getSingleResult();
    }

    public Usuario findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResult();
    }

    public Usuario findByEmail(String email) {
        return find("lower(email) = lower(?1)", email).firstResult();
    }

    @Override
    public PanacheQuery<Usuario> findAll() {
        return find("order by nome");
    }
}
