package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaDTO;
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
    public Categoria create(CategoriaDTO dto) {
        validarDados(dto, null);
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome(dto.getNome());
        novaCategoria.setTipo(converterTipo(dto.getTipo()));
        novaCategoria.setCor(dto.getCor());
        novaCategoria.setAtiva(dto.getAtiva());

        categoriaRepository.persist(novaCategoria);

        return novaCategoria;
    }

    private TipoTransacao converterTipo(String tipo) {
        try {
            return TipoTransacao.valueOf(tipo);
        } catch (IllegalArgumentException e) {
            throw ValidationException.of("tipo", "O tipo deve ser DESPESA ou RECEITA.");
        }
    }

    private void validarDados(CategoriaDTO dto, Long id) {
        Categoria categoria = categoriaRepository.findByNomeExceptId(dto.getNome(), id);
        if (categoria != null)
            throw ValidationException.of("nome", "Já existe uma categoria cadastrada com esse nome.");
    }

    @Override
    @Transactional
    public void update(long id, CategoriaDTO dto) {
        validarDados(dto, id);
        Categoria edicaoCategoria = categoriaRepository.findById(id);

        edicaoCategoria.setNome(dto.getNome());
        edicaoCategoria.setTipo(converterTipo(dto.getTipo()));
        edicaoCategoria.setCor(dto.getCor());
        edicaoCategoria.setAtiva(dto.getAtiva());
    }

    @Override
    @Transactional
    public void delete(long id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    public Categoria findById(long id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public List<Categoria> findAll(Integer page, Integer pageSize) {
        return categoriaRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Categoria> findByNome(String nome, Integer page, Integer pageSize) {
        return categoriaRepository.findByNomeLike(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return categoriaRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return categoriaRepository.findByNomeLike(nome).count();
    }

}
