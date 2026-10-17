package br.unitins.tp2.service;

import java.util.List;
import java.util.function.Consumer;

import br.unitins.tp2.dto.CadastroCompletoDTO;
import br.unitins.tp2.dto.CadastroSimplesDTO;
import br.unitins.tp2.dto.UsuarioEdicaoCompletaDTO;
import br.unitins.tp2.dto.UsuarioEdicaoSimplesDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.AssinaturaResponseMapper;
import br.unitins.tp2.mapper.PagamentoResponseMapper;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.AtividadeMei;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.model.CategoriaPadrao;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.PerfilFiscal;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.CategoriaPadraoRepository;
import br.unitins.tp2.repository.CategoriaRepository;
import br.unitins.tp2.repository.ContatoRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.TransacaoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.DocumentoUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    CategoriaPadraoRepository categoriaPadraoRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    AssinaturaService assinaturaService;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    ContatoRepository contatoRepository;

    @Inject
    MunicipioRepository municipioRepository;

    // dados fiscais e endereco ja validados (cadastro e edicao completos)
    private record DadosCompletos(String documento, String telefone, PerfilFiscal perfil, AtividadeMei atividade,
            Endereco endereco) {
    }

    @Override
    public Usuario cadastrarSimples(CadastroSimplesDTO cadastro, Papel papel) {
        String email = normalizarEmail(cadastro.email());
        validarEmailLivre(email, null);

        return criarConta(cadastro.nome(), email, cadastro.senha(), papel, usuario -> {
        });
    }

    @Override
    public Usuario cadastrarCompleto(CadastroCompletoDTO cadastro, Papel papel) {
        String email = normalizarEmail(cadastro.email());
        validarEmailLivre(email, null);
        DadosCompletos dados = montarDadosCompletos(cadastro.documento(), cadastro.telefone(),
                cadastro.idPerfilFiscal(), cadastro.idAtividadeMei(), cadastro.cep(), cadastro.logradouro(),
                cadastro.numero(), cadastro.complemento(), cadastro.bairro(), cadastro.idMunicipio());

        return criarConta(cadastro.nome(), email, cadastro.senha(), papel, usuario -> aplicar(usuario, dados));
    }

    // Cria a conta no Keycloak (login e senha, com a role do papel) e depois o perfil local, ligado
    // pelo keycloakId. Nao ha transacao entre os dois: se o perfil local falhar, a conta do Keycloak
    // e excluida. RN13: todo USER novo recebe uma copia das CategoriaPadrao nao arquivadas e a
    // assinatura no plano Gratuito; a copia e feita UMA UNICA VEZ aqui (mudancas futuras em
    // CategoriaPadrao nao afetam este usuario). O ADMIN nao tem dados pessoais nem assinatura.
    private Usuario criarConta(String nome, String email, String senha, Papel papel, Consumer<Usuario> dados) {
        String keycloakId = keycloakAdminService.criarUsuario(email, nome, senha);

        try {
            keycloakAdminService.atribuirRole(keycloakId, papel.name());

            return QuarkusTransaction.requiringNew().call(() -> {
                Usuario usuario = new Usuario();
                usuario.setKeycloakId(keycloakId);
                usuario.setNome(nome.trim());
                usuario.setEmail(email);
                usuario.setPapel(papel);
                usuario.setAtivo(true);
                dados.accept(usuario);

                usuarioRepository.persist(usuario);

                if (papel == Papel.USER) {
                    copiarCategoriasPadrao(usuario);
                    assinaturaService.criarGratuita(usuario);
                }

                return usuario;
            });
        } catch (RuntimeException e) {
            keycloakAdminService.excluirUsuario(keycloakId);
            throw e;
        }
    }

    // cadastro completo nao volta a ser simples (apagaria dados): para ele, so a edicao completa
    @Override
    public Usuario editarSimples(long id, UsuarioEdicaoSimplesDTO edicao) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        if (usuario.isCadastroCompleto()) {
            String mensagem = "Este usuário tem cadastro completo: edite-o pelo formulário completo.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("cadastroCompleto", mensagem)));
        }

        String email = normalizarEmail(edicao.email());
        validarEmailLivre(email, id);

        return atualizarConta(id, edicao.nome().trim(), email, u -> {
        });
    }

    @Override
    public Usuario editarCompleto(long id, UsuarioEdicaoCompletaDTO edicao) {
        buscarUsuarioOuFalhar(id);
        String email = normalizarEmail(edicao.email());
        validarEmailLivre(email, id);
        DadosCompletos dados = montarDadosCompletos(edicao.documento(), edicao.telefone(),
                edicao.idPerfilFiscal(), edicao.idAtividadeMei(), edicao.cep(), edicao.logradouro(),
                edicao.numero(), edicao.complemento(), edicao.bairro(), edicao.idMunicipio());

        return atualizarConta(id, edicao.nome().trim(), email, u -> aplicar(u, dados));
    }

    // Nome e e-mail tambem mudam no Keycloak (o e-mail e o login). O Keycloak muda primeiro;
    // se o perfil local falhar, ele volta ao que era.
    private Usuario atualizarConta(long id, String nome, String email, Consumer<Usuario> dados) {
        Usuario atual = buscarUsuarioOuFalhar(id);
        String keycloakId = atual.getKeycloakId();
        String nomeAntigo = atual.getNome();
        String emailAntigo = atual.getEmail();
        boolean mudaConta = keycloakId != null && (!nome.equals(nomeAntigo) || !email.equals(emailAntigo));

        if (mudaConta)
            keycloakAdminService.atualizarDados(keycloakId, email, nome);

        try {
            return QuarkusTransaction.requiringNew().call(() -> {
                Usuario usuario = buscarUsuarioOuFalhar(id);
                usuario.setNome(nome);
                usuario.setEmail(email);
                dados.accept(usuario);
                return usuario;
            });
        } catch (RuntimeException e) {
            if (mudaConta)
                keycloakAdminService.atualizarDados(keycloakId, emailAntigo, nomeAntigo);
            throw e;
        }
    }

    // snapshot das CategoriaPadrao nao arquivadas: cada uma vira uma Categoria do novo usuario,
    // independente dali para frente (editar/arquivar o padrao nao mexe mais nesta copia)
    private void copiarCategoriasPadrao(Usuario novoUsuario) {
        List<CategoriaPadrao> padroes = categoriaPadraoRepository.listNaoArquivadas();
        for (CategoriaPadrao padrao : padroes) {
            Categoria categoria = new Categoria();
            categoria.setUsuario(novoUsuario);
            categoria.setNome(padrao.getNome());
            categoria.setTipo(padrao.getTipo());
            categoria.setCor(padrao.getCor());
            categoria.setAtiva(true);
            categoriaRepository.persist(categoria);
        }
    }

    // Ativa se estiver inativo e desativa se estiver ativo, no Keycloak (desativado nao loga)
    // e no perfil local. O Keycloak muda primeiro; se o perfil local falhar, ele e revertido.
    @Override
    public Usuario alternarAtivo(long id, String keycloakIdDoAdmin) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        boolean novoAtivo = !usuario.getAtivo();

        if (!novoAtivo && usuario.getKeycloakId() != null && usuario.getKeycloakId().equals(keycloakIdDoAdmin))
            throw ValidationException.of("ativo", "Você não pode desativar a própria conta.");

        // usuario so do import.sql, sem conta no Keycloak: muda so o perfil local
        String keycloakId = usuario.getKeycloakId();
        if (keycloakId != null)
            keycloakAdminService.definirAtivo(keycloakId, novoAtivo);

        try {
            return QuarkusTransaction.requiringNew().call(() -> {
                Usuario atualizado = buscarUsuarioOuFalhar(id);
                atualizado.setAtivo(novoAtivo);
                return atualizado;
            });
        } catch (RuntimeException e) {
            if (keycloakId != null)
                keycloakAdminService.definirAtivo(keycloakId, !novoAtivo);
            throw e;
        }
    }

    @Override
    public Usuario findById(long id) {
        return buscarUsuarioOuFalhar(id);
    }

    // Visao nao invasiva para o admin: perfil, assinatura, pagamentos e so as QUANTIDADES dos
    // registros operacionais. O conteudo (valores, nomes, documentos) nunca sai daqui.
    @Override
    public UsuarioResumoResponseDTO buscarResumo(long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);

        var pagamentos = pagamentoRepository.findByUsuario(usuario.getId()).stream()
                .map(PagamentoResponseMapper::toResponse)
                .toList();

        var contagens = new UsuarioResumoResponseDTO.Contagens(
                transacaoRepository.countByUsuario(usuario.getId()),
                categoriaRepository.countByUsuario(usuario.getId()),
                contatoRepository.countByUsuario(usuario.getId()));

        return new UsuarioResumoResponseDTO(
                UsuarioResponseMapper.toResponse(usuario),
                AssinaturaResponseMapper.toResponse(assinaturaRepository.findByUsuario(usuario.getId())),
                pagamentos,
                contagens,
                transacaoRepository.findUltimaDataByUsuario(usuario.getId()));
    }

    @Override
    public List<Usuario> findByFiltros(String busca, Long idPlano, int page, int pageSize) {
        return usuarioRepository.findByFiltros(busca, idPlano).page(page, pageSize).list();
    }

    @Override
    public long count(String busca, Long idPlano) {
        return usuarioRepository.findByFiltros(busca, idPlano).count();
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }

    // o e-mail e o login: unico entre os usuarios (ignorando o proprio, na edicao)
    private void validarEmailLivre(String email, Long idUsuario) {
        Usuario dono = usuarioRepository.findByEmail(email);
        if (dono != null && !dono.getId().equals(idUsuario))
            throw ValidationException.of("email", "Já existe uma conta com esse e-mail.");
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha: documento valido
    // e compativel com o perfil (PF usa CPF; MEI e ME usam CNPJ), atividade so para MEI e o municipio
    // resolvido pela consulta do CEP.
    private DadosCompletos montarDadosCompletos(String documento, String telefone, Long idPerfil,
            Long idAtividade, String cep, String logradouro, String numero, String complemento, String bairro,
            Long idMunicipio) {
        PerfilFiscal perfil = buscarPerfilOuFalhar(idPerfil);

        if (!DocumentoUtil.isValido(documento))
            throw ValidationException.of("documento", "CPF/CNPJ inválido (dígitos verificadores não conferem).");

        boolean isCpf = documento.length() == 11;
        if (perfil == PerfilFiscal.PF && !isCpf)
            throw ValidationException.of("documento", "Pessoa física deve informar um CPF.");
        if (perfil != PerfilFiscal.PF && isCpf)
            throw ValidationException.of("documento", "MEI e microempresa devem informar um CNPJ.");

        AtividadeMei atividade = null;
        if (perfil == PerfilFiscal.MEI) {
            atividade = AtividadeMei.valueOf(idAtividade);
            if (atividade == null)
                throw ValidationException.of("idAtividadeMei", "Informe a atividade do MEI.");
        }

        Municipio municipio = municipioRepository.findById(idMunicipio);
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Município não encontrado. Consulte o CEP novamente.");

        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setLogradouro(logradouro.trim());
        endereco.setNumero(numero.trim());
        endereco.setComplemento(vazioParaNull(complemento));
        endereco.setBairro(bairro.trim());
        endereco.setMunicipio(municipio);

        return new DadosCompletos(documento, vazioParaNull(telefone), perfil, atividade, endereco);
    }

    private void aplicar(Usuario usuario, DadosCompletos dados) {
        usuario.setDocumento(dados.documento());
        usuario.setTelefone(dados.telefone());
        usuario.setPerfilFiscal(dados.perfil());
        usuario.setAtividadeMei(dados.atividade());
        usuario.setEndereco(dados.endereco());
    }

    private PerfilFiscal buscarPerfilOuFalhar(Long idPerfil) {
        PerfilFiscal perfil = PerfilFiscal.valueOf(idPerfil);
        if (perfil == null)
            throw ValidationException.of("idPerfilFiscal", "Perfil fiscal não encontrado.");

        return perfil;
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Usuario buscarUsuarioOuFalhar(long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null)
            throw new ResourceNotFoundException("Usuário não encontrado.");

        return usuario;
    }
}
