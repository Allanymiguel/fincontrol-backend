package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ContatoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Contato;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.model.TipoContato;
import br.unitins.tp2.repository.ContatoRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import br.unitins.tp2.util.DocumentoUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ContatoServiceImpl implements ContatoService {

    @Inject
    ContatoRepository contatoRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Override
    @Transactional
    public Contato create(ContatoRequestDTO contato) {
        validarDados(contato, null);

        Contato novoContato = new Contato();
        aplicarDados(novoContato, contato);

        contatoRepository.persist(novoContato);

        return novoContato;
    }

    @Override
    @Transactional
    public void update(long id, ContatoRequestDTO contato) {
        Contato edicaoContato = buscarContatoOuFalhar(id);
        validarDados(contato, id);
        aplicarDados(edicaoContato, contato);
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!contatoRepository.deleteById(id))
            throw new ResourceNotFoundException("Contato não encontrado.");
    }

    @Override
    public Contato findById(long id) {
        return buscarContatoOuFalhar(id);
    }

    @Override
    public List<Contato> findAll(int page, int pageSize) {
        return contatoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Contato> findByNome(String nome, int page, int pageSize) {
        return contatoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return contatoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return contatoRepository.findByNome(nome).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(ContatoRequestDTO dto, Long idContato) {
        if (!DocumentoUtil.isValido(dto.documento()))
            throw ValidationException.of("documento", "CPF/CNPJ inválido (dígitos verificadores não conferem).");

        if (contatoRepository.findByDocumentoExceptId(dto.documento(), idContato) != null)
            throw ValidationException.of("documento", "Já existe um contato cadastrado com esse CPF/CNPJ.");
    }

    private void aplicarDados(Contato destino, ContatoRequestDTO origem) {
        destino.setNome(origem.nome().trim());
        destino.setTipo(buscarTipoOuFalhar(origem.idTipo()));
        destino.setDocumento(origem.documento());
        destino.setEmail(vazioParaNull(origem.email()));
        destino.setTelefone(vazioParaNull(origem.telefone()));

        Endereco endereco = destino.getEndereco() != null ? destino.getEndereco() : new Endereco();
        endereco.setCep(origem.cep());
        endereco.setLogradouro(origem.logradouro().trim());
        endereco.setNumero(origem.numero().trim());
        endereco.setComplemento(vazioParaNull(origem.complemento()));
        endereco.setBairro(origem.bairro().trim());
        endereco.setMunicipio(buscarMunicipioOuFalhar(origem.idMunicipio()));
        destino.setEndereco(endereco);
    }

    private TipoContato buscarTipoOuFalhar(Long idTipo) {
        TipoContato tipo = TipoContato.valueOf(idTipo);
        if (tipo == null)
            throw ValidationException.of("idTipo", "Tipo de contato não encontrado.");

        return tipo;
    }

    private Municipio buscarMunicipioOuFalhar(Long idMunicipio) {
        Municipio municipio = municipioRepository.findById(idMunicipio);
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Município não encontrado. Consulte o CEP novamente.");

        return municipio;
    }

    private Contato buscarContatoOuFalhar(long id) {
        Contato contato = contatoRepository.findById(id);
        if (contato == null)
            throw new ResourceNotFoundException("Contato não encontrado.");

        return contato;
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
