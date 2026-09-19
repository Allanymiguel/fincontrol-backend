package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.TransacaoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.model.EscopoTransacao;
import br.unitins.tp2.model.TipoTransacao;
import br.unitins.tp2.model.Transacao;
import br.unitins.tp2.repository.CategoriaRepository;
import br.unitins.tp2.repository.TransacaoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransacaoServiceImpl implements TransacaoService {

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public Transacao create(TransacaoRequestDTO transacao) {
        Transacao novaTransacao = new Transacao();
        aplicarDados(novaTransacao, transacao);

        transacaoRepository.persist(novaTransacao);

        return novaTransacao;
    }

    @Override
    @Transactional
    public void update(long id, TransacaoRequestDTO transacao) {
        Transacao edicaoTransacao = buscarTransacaoOuFalhar(id);
        aplicarDados(edicaoTransacao, transacao);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!transacaoRepository.deleteById(id))
            throw new ResourceNotFoundException("Transação não encontrada.");
    }

    @Override
    public Transacao findById(long id) {
        return buscarTransacaoOuFalhar(id);
    }

    @Override
    public List<Transacao> findAll(int page, int pageSize) {
        return transacaoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Transacao> findByDescricao(String descricao, int page, int pageSize) {
        return transacaoRepository.findByDescricao(descricao).page(page, pageSize).list();
    }

    @Override
    public List<Transacao> findByCategoria(Long idCategoria, int page, int pageSize) {
        return transacaoRepository.findByCategoria(idCategoria).page(page, pageSize).list();
    }

    @Override
    public List<Transacao> findByDescricaoAndCategoria(String descricao, Long idCategoria, int page, int pageSize) {
        return transacaoRepository.findByDescricaoAndCategoria(descricao, idCategoria).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return transacaoRepository.findAll().count();
    }

    @Override
    public long count(String descricao) {
        return transacaoRepository.findByDescricao(descricao).count();
    }

    @Override
    public long countByCategoria(Long idCategoria) {
        return transacaoRepository.findByCategoria(idCategoria).count();
    }

    @Override
    public long count(String descricao, Long idCategoria) {
        return transacaoRepository.findByDescricaoAndCategoria(descricao, idCategoria).count();
    }

    private void aplicarDados(Transacao destino, TransacaoRequestDTO origem) {
        destino.setDescricao(origem.descricao());
        destino.setValor(origem.valor());
        destino.setData(origem.data());
        destino.setTipo(buscarTipoOuFalhar(origem.idTipo()));
        destino.setEscopo(buscarEscopoOuFalhar(origem.idEscopo()));
        destino.setCategoria(buscarCategoriaOuFalhar(origem.idCategoria()));
    }

    private TipoTransacao buscarTipoOuFalhar(Long idTipo) {
        TipoTransacao tipo = TipoTransacao.valueOf(idTipo);
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de transação não encontrado.");

        return tipo;
    }

    private EscopoTransacao buscarEscopoOuFalhar(Long idEscopo) {
        EscopoTransacao escopo = EscopoTransacao.valueOf(idEscopo);
        if (escopo == null)
            throw ValidationException.of("idEscopo", "Escopo de transação não encontrado.");

        return escopo;
    }

    private Categoria buscarCategoriaOuFalhar(Long idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria);
        if (categoria == null)
            throw ValidationException.of("idCategoria", "Categoria não encontrada.");

        return categoria;
    }

    private Transacao buscarTransacaoOuFalhar(long id) {
        Transacao transacao = transacaoRepository.findById(id);
        if (transacao == null)
            throw new ResourceNotFoundException("Transação não encontrada.");

        return transacao;
    }
}
