package br.unitins.tp2.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.CupomRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.CupomRepository;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CupomServiceImpl implements CupomService {

    @Inject
    CupomRepository cupomRepository;

    @Inject
    AuditoriaService auditoria;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    PlanoRepository planoRepository;

    @Override
    @Transactional
    public Cupom create(CupomRequestDTO cupom) {
        validarDados(cupom, null);

        Cupom novoCupom = new Cupom();
        aplicarDados(novoCupom, cupom);

        cupomRepository.persist(novoCupom);
        auditoria.registrar(AcaoAuditoria.CRIACAO_CUPOM, TipoAlvoAuditoria.CUPOM, novoCupom.getId(),
                novoCupom.getCodigo(), Map.of(), RetratoAuditoria.de(novoCupom));

        return novoCupom;
    }

    @Override
    @Transactional
    public void update(long id, CupomRequestDTO cupom) {
        Cupom edicaoCupom = buscarCupomOuFalhar(id);
        validarDados(cupom, id);
        Map<String, String> antes = RetratoAuditoria.de(edicaoCupom);
        aplicarDados(edicaoCupom, cupom);
        auditoria.registrar(AcaoAuditoria.ALTERACAO_CUPOM, TipoAlvoAuditoria.CUPOM, id,
                edicaoCupom.getCodigo(), antes, RetratoAuditoria.de(edicaoCupom));
    }

    // cupom em uso numa assinatura ou no historico de pagamentos nao pode ser excluido: o admin o desativa
    @Override
    @Transactional
    public void delete(long id) {
        // a mensagem vai no detail para aparecer no aviso do front (nao ha campo ligado a exclusao)
        if (assinaturaRepository.countByCupom(id) > 0 || pagamentoRepository.countByCupom(id) > 0) {
            String mensagem = "Este cupom já foi usado em assinaturas e não pode ser excluído. Desative-o.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("ativo", mensagem)));
        }

        Cupom excluido = buscarCupomOuFalhar(id);
        Map<String, String> antes = RetratoAuditoria.de(excluido);
        cupomRepository.delete(excluido);
        auditoria.registrar(AcaoAuditoria.EXCLUSAO_CUPOM, TipoAlvoAuditoria.CUPOM, id,
                excluido.getCodigo(), antes, Map.of());
    }

    @Override
    public Cupom findById(long id) {
        return buscarCupomOuFalhar(id);
    }

    @Override
    public List<Cupom> findAll(int page, int pageSize) {
        return cupomRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Cupom> findByCodigo(String codigo, int page, int pageSize) {
        return cupomRepository.findByCodigo(codigo).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return cupomRepository.findAll().count();
    }

    @Override
    public long count(String codigo) {
        return cupomRepository.findByCodigo(codigo).count();
    }

    @Override
    public Metricas metricas(Cupom cupom) {
        Object[] pagamentos = cupomRepository.pagamentosEDesconto(cupom.getId());
        return new Metricas(cupomRepository.countAdesoes(cupom.getId()), (Long) pagamentos[0],
                CupomRepository.comoValor(pagamentos[1]).setScale(2));
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(CupomRequestDTO dto, Long idCupom) {
        if (cupomRepository.findByCodigoExceptId(normalizarCodigo(dto.codigo()), idCupom) != null)
            throw ValidationException.of("codigo", "Já existe um cupom cadastrado com esse código.");

        // o limite nao pode ficar abaixo das adesoes que ja aconteceram
        if (idCupom != null && dto.limiteUsos() != null) {
            long usos = cupomRepository.countAdesoes(idCupom);
            if (dto.limiteUsos() < usos)
                throw ValidationException.of("limiteUsos", "O cupom já foi usado " + usos
                        + (usos == 1 ? " vez" : " vezes") + ": o limite não pode ser menor que isso.");
        }
    }

    private void aplicarDados(Cupom destino, CupomRequestDTO origem) {
        destino.setCodigo(normalizarCodigo(origem.codigo()));
        destino.setPercentualDesconto(origem.percentualDesconto());
        destino.setValidade(origem.validade());
        destino.setAtivo(origem.ativo());
        destino.setLimiteUsos(origem.limiteUsos());
        destino.getPlanos().clear();
        destino.getPlanos().addAll(buscarPlanosOuFalhar(origem.idPlanos()));
    }

    // vazio = todos os planos; o Gratuito nao entra (nao tem preco para descontar)
    private LinkedHashSet<Plano> buscarPlanosOuFalhar(List<Long> idPlanos) {
        LinkedHashSet<Plano> planos = new LinkedHashSet<>();
        if (idPlanos == null)
            return planos;
        for (Long idPlano : idPlanos) {
            Plano plano = idPlano == null ? null : planoRepository.findById(idPlano);
            if (plano == null)
                throw ValidationException.of("idPlanos", "Plano não encontrado.");
            if (plano.isGratuito())
                throw ValidationException.of("idPlanos", "O plano Gratuito não tem preço para descontar.");
            planos.add(plano);
        }
        return planos;
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private Cupom buscarCupomOuFalhar(long id) {
        Cupom cupom = cupomRepository.findById(id);
        if (cupom == null)
            throw new ResourceNotFoundException("Cupom não encontrado.");

        return cupom;
    }
}
