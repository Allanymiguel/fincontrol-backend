package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.RegistroRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AtividadeMei;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.PerfilFiscal;
import br.unitins.tp2.model.Usuario;
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

    // Cria a conta no Keycloak (login e senha) e depois o perfil local, ligado pelo keycloakId.
    // Nao ha transacao entre os dois: se o perfil local falhar, o usuario do Keycloak e excluido.
    // TODO (RN13): copiar as CategoriaPadrao nao arquivadas e criar a Assinatura no plano gratuito,
    // quando Categoria tiver dono (RN12) e a Assinatura existir.
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
                return usuario;
            });
        } catch (RuntimeException e) {
            keycloakAdminService.excluirUsuario(keycloakId);
            throw e;
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
