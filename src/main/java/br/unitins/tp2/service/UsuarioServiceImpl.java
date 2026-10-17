package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.AssinaturaResponseDTO;
import br.unitins.tp2.dto.RegistroRequestDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.AssinaturaResponseMapper;
import br.unitins.tp2.mapper.UsuarioResponseMapper;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.AtividadeMei;
import br.unitins.tp2.model.Categoria;
import br.unitins.tp2.model.CategoriaPadrao;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.PerfilFiscal;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.CategoriaPadraoRepository;
import br.unitins.tp2.repository.CategoriaRepository;
import br.unitins.tp2.repository.ContatoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.TransacaoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.DocumentoUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    // nome do plano aplicado no cadastro (RN13). Precisa existir no banco (seed ou cadastrado pelo admin).
    private static final String NOME_PLANO_GRATUITO = "Gratuito";

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    CategoriaPadraoRepository categoriaPadraoRepository;

    @Inject
    CategoriaRepository categoriaRepository;

    @Inject
    PlanoRepository planoRepository;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    TransacaoRepository transacaoRepository;

    @Inject
    ContatoRepository contatoRepository;

    // Cria a conta no Keycloak (login e senha) e depois o perfil local, ligado pelo keycloakId.
    // Nao ha transacao entre os dois: se o perfil local falhar, o usuario do Keycloak e excluido.
    // RN13: toda conta nova recebe uma copia das CategoriaPadrao nao arquivadas e uma Assinatura
    // ATIVA no plano Gratuito. A copia e feita UMA UNICA VEZ aqui; mudancas futuras em
    // CategoriaPadrao nao afetam este usuario (nem reinserem o que ele apagar).
    @Override
    public Usuario registrar(RegistroRequestDTO registro) {
        String email = registro.email().trim().toLowerCase();
        PerfilFiscal perfil = buscarPerfilOuFalhar(registro.idPerfilFiscal());
        AtividadeMei atividade = validarDados(registro, email, perfil);

        String keycloakId = keycloakAdminService.criarUsuario(email, registro.nome(), registro.senha());

        try {
            keycloakAdminService.atribuirRole(keycloakId, Papel.USER.name());

            return QuarkusTransaction.requiringNew().call(() -> {
                Usuario usuario = new Usuario();
                usuario.setKeycloakId(keycloakId);
                usuario.setNome(registro.nome().trim());
                usuario.setEmail(email);
                usuario.setDocumento(registro.documento());
                usuario.setTelefone(vazioParaNull(registro.telefone()));
                usuario.setPerfilFiscal(perfil);
                usuario.setAtividadeMei(atividade);
                usuario.setPapel(Papel.USER);
                usuario.setAtivo(true);

                usuarioRepository.persist(usuario);

                copiarCategoriasPadrao(usuario);
                criarAssinaturaGratuita(usuario);

                return usuario;
            });
        } catch (RuntimeException e) {
            keycloakAdminService.excluirUsuario(keycloakId);
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

    // Assinatura ATIVA no plano Gratuito. Sem o plano configurado, o cadastro falha
    // e o usuario do Keycloak e desfeito (pelo catch do registrar).
    private void criarAssinaturaGratuita(Usuario novoUsuario) {
        Plano gratuito = planoRepository.findByNomeExato(NOME_PLANO_GRATUITO);
        if (gratuito == null)
            throw ValidationException.of("idPlano",
                    "Plano \"" + NOME_PLANO_GRATUITO + "\" não encontrado. Configure-o antes de aceitar cadastros.");

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(novoUsuario);
        assinatura.setPlano(gratuito);
        assinatura.setCupom(null);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDataFim(null);
        assinatura.setPeriodicidade(Periodicidade.MENSAL);
        assinatura.setValorPago(gratuito.getPrecoMensal());
        assinatura.setFormaPagamento(FormaPagamento.NENHUMA);
        assinatura.setObservacao("Plano gratuito aplicado no cadastro");
        assinaturaRepository.persist(assinatura);
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

    // Visao nao invasiva para o admin: perfil, assinaturas e so as QUANTIDADES dos
    // registros operacionais. O conteudo (valores, nomes, documentos) nunca sai daqui.
    @Override
    public UsuarioResumoResponseDTO buscarResumo(long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);

        List<AssinaturaResponseDTO> assinaturas = assinaturaRepository.findByUsuario(usuario.getId()).stream()
                .map(AssinaturaResponseMapper::toResponse)
                .toList();
        AssinaturaResponseDTO atual = assinaturas.stream()
                .filter(a -> a.status() == StatusAssinatura.ATIVA)
                .findFirst()
                .orElse(null);

        var contagens = new UsuarioResumoResponseDTO.Contagens(
                transacaoRepository.countByUsuario(usuario.getId()),
                categoriaRepository.countByUsuario(usuario.getId()),
                contatoRepository.countByUsuario(usuario.getId()));

        return new UsuarioResumoResponseDTO(
                UsuarioResponseMapper.toResponse(usuario),
                atual,
                assinaturas,
                contagens,
                transacaoRepository.findUltimaDataByUsuario(usuario.getId()));
    }

    @Override
    public List<Usuario> findAll(int page, int pageSize) {
        return usuarioRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Usuario> findByNomeOuEmail(String busca, int page, int pageSize) {
        return usuarioRepository.findByNomeOuEmail(busca).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return usuarioRepository.findAll().count();
    }

    @Override
    public long count(String busca) {
        return usuarioRepository.findByNomeOuEmail(busca).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha.
    // Devolve a atividade do MEI (nula para os outros perfis).
    private AtividadeMei validarDados(RegistroRequestDTO dto, String email, PerfilFiscal perfil) {
        if (usuarioRepository.findByEmail(email) != null)
            throw ValidationException.of("email", "Já existe uma conta com esse e-mail.");

        if (!DocumentoUtil.isValido(dto.documento()))
            throw ValidationException.of("documento", "CPF/CNPJ inválido (dígitos verificadores não conferem).");

        // PF usa CPF; MEI e ME sao empresas e usam CNPJ
        boolean isCpf = dto.documento().length() == 11;
        if (perfil == PerfilFiscal.PF && !isCpf)
            throw ValidationException.of("documento", "Pessoa física deve informar um CPF.");
        if (perfil != PerfilFiscal.PF && isCpf)
            throw ValidationException.of("documento", "MEI e microempresa devem informar um CNPJ.");

        if (perfil != PerfilFiscal.MEI)
            return null;

        AtividadeMei atividade = AtividadeMei.valueOf(dto.idAtividadeMei());
        if (atividade == null)
            throw ValidationException.of("idAtividadeMei", "Informe a atividade do MEI.");

        return atividade;
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
