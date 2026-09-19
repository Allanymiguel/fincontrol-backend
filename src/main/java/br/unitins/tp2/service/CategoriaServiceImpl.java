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

    @Override
    @Transactional
    public Categoria create(CategoriaRequestDTO categoria) {
        Categoria novaCategoria = new Categoria();
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
        if (!categoriaRepository.deleteById(id))
            throw new ResourceNotFoundException("Categoria não encontrada.");
    }

    @Override
    public Categoria findById(long id) {
        return buscarCategoriaOuFalhar(id);
    }

    @Override
    public List<Categoria> findAll(int page, int pageSize) {
        return categoriaRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Categoria> findByNome(String nome, int page, int pageSize) {
        return categoriaRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return categoriaRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return categoriaRepository.findByNome(nome).count();
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

    private Categoria buscarCategoriaOuFalhar(long id) {
        Categoria categoria = categoriaRepository.findById(id);
        if (categoria == null)
            throw new ResourceNotFoundException("Categoria não encontrada.");

        return categoria;
    }
}
