package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CategoriaPadraoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.CategoriaPadrao;
import br.unitins.tp2.model.TipoTransacao;
import br.unitins.tp2.repository.CategoriaPadraoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CategoriaPadraoServiceImpl implements CategoriaPadraoService {

    @Inject
    CategoriaPadraoRepository categoriaPadraoRepository;

    @Override
    @Transactional
    public CategoriaPadrao create(CategoriaPadraoRequestDTO categoria) {
        TipoTransacao tipo = buscarTipoOuFalhar(categoria.idTipo());
        validarDados(categoria, tipo, null);

        CategoriaPadrao novaCategoria = new CategoriaPadrao();
        aplicarDados(novaCategoria, categoria, tipo);
        novaCategoria.setArquivada(false);

        categoriaPadraoRepository.persist(novaCategoria);

        return novaCategoria;
    }

    @Override
    @Transactional
    public void update(long id, CategoriaPadraoRequestDTO categoria) {
        CategoriaPadrao edicaoCategoria = buscarCategoriaOuFalhar(id);
        TipoTransacao tipo = buscarTipoOuFalhar(categoria.idTipo());
        validarDados(categoria, tipo, id);
        aplicarDados(edicaoCategoria, categoria, tipo);
    }

    // arquiva se estiver ativa e desarquiva se estiver arquivada
    @Override
    @Transactional
    public CategoriaPadrao alternarArquivada(long id) {
        CategoriaPadrao categoria = buscarCategoriaOuFalhar(id);
        categoria.setArquivada(!categoria.getArquivada());

        return categoria;
    }

    @Override
    public CategoriaPadrao findById(long id) {
        return buscarCategoriaOuFalhar(id);
    }

    @Override
    public List<CategoriaPadrao> findByFiltros(String nome, Long idTipo, int page, int pageSize) {
        return categoriaPadraoRepository.findByFiltros(nome, buscarTipoDoFiltro(idTipo)).page(page, pageSize).list();
    }

    @Override
    public long count(String nome, Long idTipo) {
        return categoriaPadraoRepository.findByFiltros(nome, buscarTipoDoFiltro(idTipo)).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(CategoriaPadraoRequestDTO dto, TipoTransacao tipo, Long idCategoria) {
        if (categoriaPadraoRepository.findByNomeAndTipoExceptId(dto.nome().trim(), tipo, idCategoria) != null)
            throw ValidationException.of("nome", "Já existe uma categoria padrão de " + tipo.getNome().toLowerCase()
                    + " com esse nome.");
    }

    private void aplicarDados(CategoriaPadrao destino, CategoriaPadraoRequestDTO origem, TipoTransacao tipo) {
        destino.setNome(origem.nome().trim());
        destino.setTipo(tipo);
        destino.setCor(origem.cor() == null ? null : origem.cor().toUpperCase());
    }

    // no filtro da listagem o tipo e opcional, mas se vier precisa existir
    private TipoTransacao buscarTipoDoFiltro(Long idTipo) {
        return idTipo == null ? null : buscarTipoOuFalhar(idTipo);
    }

    private TipoTransacao buscarTipoOuFalhar(Long idTipo) {
        TipoTransacao tipo = TipoTransacao.valueOf(idTipo);
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de transação não encontrado.");

        return tipo;
    }

    private CategoriaPadrao buscarCategoriaOuFalhar(long id) {
        CategoriaPadrao categoria = categoriaPadraoRepository.findById(id);
        if (categoria == null)
            throw new ResourceNotFoundException("Categoria padrão não encontrada.");

        return categoria;
    }
}
