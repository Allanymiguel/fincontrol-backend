package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.ContatoCompletoDTO;
import br.unitins.tp2.dto.ContatoSimplesDTO;
import br.unitins.tp2.exception.Problem;
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

    @Inject
    UsuarioLogadoService usuarioLogadoService;

    // simples: so nome, tipo, telefone e e-mail (sem CPF/CNPJ e endereco)
    @Override
    @Transactional
    public Contato createSimples(ContatoSimplesDTO contato) {
        Contato novoContato = new Contato();
        novoContato.setUsuario(usuarioLogadoService.atual());
        aplicarDadosBasicos(novoContato, contato.nome(), contato.idTipo(), contato.email(), contato.telefone());

        contatoRepository.persist(novoContato);

        return novoContato;
    }

    @Override
    @Transactional
    public Contato createCompleto(ContatoCompletoDTO contato) {
        long idUsuario = usuarioLogadoService.idAtual();
        validarDocumento(contato, null, idUsuario);

        Contato novoContato = new Contato();
        novoContato.setUsuario(usuarioLogadoService.atual());
        aplicarDadosCompletos(novoContato, contato);

        contatoRepository.persist(novoContato);

        return novoContato;
    }

    // contato completo nao volta a ser simples (apagaria o documento e o endereco)
    @Override
    @Transactional
    public void updateSimples(long id, ContatoSimplesDTO contato) {
        Contato edicaoContato = buscarContatoOuFalhar(id);
        if (edicaoContato.isCadastroCompleto()) {
            String mensagem = "Este contato tem cadastro completo: edite-o pelo formulário completo.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("cadastroCompleto", mensagem)));
        }

        aplicarDadosBasicos(edicaoContato, contato.nome(), contato.idTipo(), contato.email(), contato.telefone());
    }

    // completa um contato simples ou atualiza um completo
    @Override
    @Transactional
    public void updateCompleto(long id, ContatoCompletoDTO contato) {
        Contato edicaoContato = buscarContatoOuFalhar(id);
        validarDocumento(contato, id, edicaoContato.getUsuario().getId());
        aplicarDadosCompletos(edicaoContato, contato);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Contato contato = buscarContatoOuFalhar(id);
        contatoRepository.delete(contato);
    }

    @Override
    public Contato findById(long id) {
        return buscarContatoOuFalhar(id);
    }

    @Override
    public List<Contato> findAll(int page, int pageSize) {
        return contatoRepository.findAllByUsuario(usuarioLogadoService.idAtual()).page(page, pageSize).list();
    }

    @Override
    public List<Contato> findByNome(String nome, int page, int pageSize) {
        return contatoRepository.findByNomeAndUsuario(nome, usuarioLogadoService.idAtual()).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return contatoRepository.countByUsuario(usuarioLogadoService.idAtual());
    }

    @Override
    public long count(String nome) {
        return contatoRepository.findByNomeAndUsuario(nome, usuarioLogadoService.idAtual()).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha.
    // O documento e unico por usuario, nao globalmente (dois usuarios podem ter o mesmo fornecedor).
    private void validarDocumento(ContatoCompletoDTO dto, Long idContato, long idUsuario) {
        if (!DocumentoUtil.isValido(dto.documento()))
            throw ValidationException.of("documento", "CPF/CNPJ inválido (dígitos verificadores não conferem).");

        if (contatoRepository.findByDocumentoExceptIdAndUsuario(dto.documento(), idContato, idUsuario) != null)
            throw ValidationException.of("documento", "Já existe um contato cadastrado com esse CPF/CNPJ.");
    }

    // campos das duas formas de cadastro
    private void aplicarDadosBasicos(Contato destino, String nome, Long idTipo, String email, String telefone) {
        destino.setNome(nome.trim());
        destino.setTipo(buscarTipoOuFalhar(idTipo));
        destino.setEmail(vazioParaNull(email));
        destino.setTelefone(vazioParaNull(telefone));
    }

    private void aplicarDadosCompletos(Contato destino, ContatoCompletoDTO origem) {
        aplicarDadosBasicos(destino, origem.nome(), origem.idTipo(), origem.email(), origem.telefone());
        destino.setDocumento(origem.documento());

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
        Contato contato = contatoRepository.findByIdAndUsuario(id, usuarioLogadoService.idAtual());
        if (contato == null)
            throw new ResourceNotFoundException("Contato não encontrado.");

        return contato;
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
