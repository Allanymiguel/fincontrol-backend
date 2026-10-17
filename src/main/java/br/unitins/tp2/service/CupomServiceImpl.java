package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CupomRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.CupomRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CupomServiceImpl implements CupomService {

    @Inject
    CupomRepository cupomRepository;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Override
    @Transactional
    public Cupom create(CupomRequestDTO cupom) {
        validarDados(cupom, null);

        Cupom novoCupom = new Cupom();
        aplicarDados(novoCupom, cupom);

        cupomRepository.persist(novoCupom);

        return novoCupom;
    }

    @Override
    @Transactional
    public void update(long id, CupomRequestDTO cupom) {
        Cupom edicaoCupom = buscarCupomOuFalhar(id);
        validarDados(cupom, id);
        aplicarDados(edicaoCupom, cupom);
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

        if (!cupomRepository.deleteById(id))
            throw new ResourceNotFoundException("Cupom não encontrado.");
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

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(CupomRequestDTO dto, Long idCupom) {
        if (cupomRepository.findByCodigoExceptId(normalizarCodigo(dto.codigo()), idCupom) != null)
            throw ValidationException.of("codigo", "Já existe um cupom cadastrado com esse código.");
    }

    private void aplicarDados(Cupom destino, CupomRequestDTO origem) {
        destino.setCodigo(normalizarCodigo(origem.codigo()));
        destino.setPercentualDesconto(origem.percentualDesconto());
        destino.setValidade(origem.validade());
        destino.setAtivo(origem.ativo());
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
