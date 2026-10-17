package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.model.TipoTransacao;
import br.unitins.tp2.repository.CategoriaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoriaServiceImpl implements CategoriaService {

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    UsuarioLogadoService usuarioLogadoService;

    @Override
    @Transactional
    public Categoria create(CategoriaRequestDTO categoria) {
        Categoria novaCategoria = new Categoria();
        novaCategoria.setUsuario(usuarioLogadoService.atual());
        aplicarDados(novaCategoria, categoria);

        categoriaRepository.persist(novaCategoria);

        return novaCategoria;
    }

    @Override
    @Transactional
    public void update(long id, CategoriaRequestDTO categoria) {
        Categoria edicaoCategoria = buscarCategoriaOuFalhar(id);
        aplicarDados(edicaoCategoria, categoria);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Categoria categoria = buscarCategoriaOuFalhar(id);
        categoriaRepository.delete(categoria);
    }

    @Override
    public Categoria findById(long id) {
        return buscarCategoriaOuFalhar(id);
    }

    @Override
    public List<Categoria> findAll(int page, int pageSize) {
        return categoriaRepository.findAllByUsuario(usuarioLogadoService.idAtual()).page(page, pageSize).list();
    }

    @Override
    public List<Categoria> findByNome(String nome, int page, int pageSize) {
        return categoriaRepository.findByNomeAndUsuario(nome, usuarioLogadoService.idAtual())
                .page(page, pageSize).list();
    }

    @Override
    public long count() {
        return categoriaRepository.countByUsuario(usuarioLogadoService.idAtual());
    }

    @Override
    public long count(String nome) {
        return categoriaRepository.findByNomeAndUsuario(nome, usuarioLogadoService.idAtual()).count();
    }

    private void aplicarDados(Categoria destino, CategoriaRequestDTO origem) {
        destino.setNome(origem.nome());
        destino.setTipo(buscarTipoOuFalhar(origem.idTipo()));
        destino.setCor(origem.cor());
        destino.setAtiva(origem.ativa());
    }

    private TipoTransacao buscarTipoOuFalhar(Long idTipo) {
        TipoTransacao tipo = TipoTransacao.valueOf(idTipo);
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de transação não encontrado.");

        return tipo;
    }

    // buscar escopado ao usuario logado: nao encontrado = 404 (mesmo que exista para outro dono)
    private Categoria buscarCategoriaOuFalhar(long id) {
        Categoria categoria = categoriaRepository.findByIdAndUsuario(id, usuarioLogadoService.idAtual());
        if (categoria == null)
            throw new ResourceNotFoundException("Categoria não encontrada.");

        return categoria;
    }
}
