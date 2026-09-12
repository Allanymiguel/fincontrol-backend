package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.TransacaoDTO;
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
    public Transacao create(TransacaoDTO dto) {
        Categoria categoria = buscarCategoria(dto.getIdCategoria());

        Transacao novaTransacao = new Transacao();
        novaTransacao.setDescricao(dto.getDescricao());
        novaTransacao.setValor(dto.getValor());
        novaTransacao.setData(dto.getData());
        novaTransacao.setTipo(converterEnum(TipoTransacao.class, dto.getTipo(), "tipo", "DESPESA ou RECEITA"));
        novaTransacao.setEscopo(converterEnum(EscopoTransacao.class, dto.getEscopo(), "escopo", "PESSOAL ou EMPRESA"));
        novaTransacao.setCategoria(categoria);

        transacaoRepository.persist(novaTransacao);

        return novaTransacao;
    }

    private Categoria buscarCategoria(Long idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria);
        if (categoria == null)
            throw ValidationException.of("idCategoria", "Categoria não encontrada.");
        return categoria;
    }

    private <T extends Enum<T>> T converterEnum(Class<T> tipoEnum, String valor, String campo, String valoresValidos) {
        try {
            return Enum.valueOf(tipoEnum, valor);
        } catch (IllegalArgumentException e) {
            throw ValidationException.of(campo, "O campo " + campo + " deve ser " + valoresValidos + ".");
        }
    }

    @Override
    @Transactional
    public void update(long id, TransacaoDTO dto) {
        Categoria categoria = buscarCategoria(dto.getIdCategoria());
        Transacao edicaoTransacao = transacaoRepository.findById(id);

        edicaoTransacao.setDescricao(dto.getDescricao());
        edicaoTransacao.setValor(dto.getValor());
        edicaoTransacao.setData(dto.getData());
        edicaoTransacao.setTipo(converterEnum(TipoTransacao.class, dto.getTipo(), "tipo", "DESPESA ou RECEITA"));
        edicaoTransacao.setEscopo(converterEnum(EscopoTransacao.class, dto.getEscopo(), "escopo", "PESSOAL ou EMPRESA"));
        edicaoTransacao.setCategoria(categoria);
    }

    @Override
    @Transactional
    public void delete(long id) {
        transacaoRepository.deleteById(id);
    }

    @Override
    public Transacao findById(long id) {
        return transacaoRepository.findById(id);
    }

    @Override
    public List<Transacao> findAll(Integer page, Integer pageSize) {
        return transacaoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Transacao> findByDescricao(String descricao, Integer page, Integer pageSize) {
        return transacaoRepository.findByDescricao(descricao).page(page, pageSize).list();
    }

    @Override
    public List<Transacao> findByCategoria(Long idCategoria, Integer page, Integer pageSize) {
        return transacaoRepository.findByCategoria(idCategoria).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return transacaoRepository.findAll().count();
    }

    @Override
    public long count(String descricao) {
        return transacaoRepository.findByDescricao(descricao).count();
    }

}
