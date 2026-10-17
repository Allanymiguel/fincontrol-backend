package br.unitins.tp2.repository;

import java.util.List;

import br.unitins.tp2.model.Pagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PagamentoRepository implements PanacheRepository<Pagamento> {

    // historico de cobrancas do usuario, da mais recente para a mais antiga
    public List<Pagamento> findByUsuario(Long idUsuario) {
        return list("assinatura.usuario.id = ?1 order by dataPagamento desc, id desc", idUsuario);
    }

    // usados para impedir a exclusao de plano/cupom que ja aparecem no historico
    public long countByPlano(Long idPlano) {
        return count("plano.id", idPlano);
    }

    public long countByCupom(Long idCupom) {
        return count("cupom.id", idCupom);
    }
}
