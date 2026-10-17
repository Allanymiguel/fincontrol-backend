package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlanoServiceImpl implements PlanoService {

    @Inject
    PlanoRepository planoRepository;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Override
    @Transactional
    public Plano create(PlanoRequestDTO plano) {
        validarDados(plano, null);

        Plano novoPlano = new Plano();
        aplicarDados(novoPlano, plano);

        planoRepository.persist(novoPlano);

        return novoPlano;
    }

    @Override
    @Transactional
    public void update(long id, PlanoRequestDTO plano) {
        Plano edicaoPlano = buscarPlanoOuFalhar(id);
        validarDados(plano, id);
        aplicarDados(edicaoPlano, plano);
    }

    // plano em uso (vigente ou agendado) ou no historico de pagamentos nao pode ser excluido: o admin o desativa
    @Override
    @Transactional
    public void delete(long id) {
        // a mensagem vai no detail para aparecer no aviso do front (nao ha campo ligado a exclusao)
        if (assinaturaRepository.countByPlano(id) > 0 || pagamentoRepository.countByPlano(id) > 0) {
            String mensagem = "Este plano tem assinaturas e não pode ser excluído. Desative-o para não aceitar novas.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("ativo", mensagem)));
        }

        if (!planoRepository.deleteById(id))
            throw new ResourceNotFoundException("Plano não encontrado.");
    }

    @Override
    public Plano findById(long id) {
        return buscarPlanoOuFalhar(id);
    }

    @Override
    public List<Plano> findAll(int page, int pageSize) {
        return planoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Plano> findByNome(String nome, int page, int pageSize) {
        return planoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return planoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return planoRepository.findByNome(nome).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(PlanoRequestDTO dto, Long idPlano) {
        if (planoRepository.findByNomeExceptId(dto.nome().trim(), idPlano) != null)
            throw ValidationException.of("nome", "Já existe um plano cadastrado com esse nome.");
    }

    private void aplicarDados(Plano destino, PlanoRequestDTO origem) {
        destino.setNome(origem.nome().trim());
        destino.setPrecoMensal(origem.precoMensal());
        destino.setPrecoAnual(origem.precoAnual());
        destino.setLimiteTransacoes(origem.limiteTransacoes());
        destino.setLimiteMetas(origem.limiteMetas());
        destino.setLiberaRecorrencias(origem.liberaRecorrencias());
        destino.setLiberaFiscal(origem.liberaFiscal());
        destino.setLiberaExportacao(origem.liberaExportacao());
        destino.setAtivo(origem.ativo());
    }

    private Plano buscarPlanoOuFalhar(long id) {
        Plano plano = planoRepository.findById(id);
        if (plano == null)
            throw new ResourceNotFoundException("Plano não encontrado.");

        return plano;
    }
}
