package br.unitins.tp2.service;

import java.util.List;
import java.util.function.Function;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.unitins.tp2.client.KeycloakAdminClient;
import br.unitins.tp2.client.KeycloakErro;
import br.unitins.tp2.client.KeycloakRole;
import br.unitins.tp2.client.KeycloakSessao;
import br.unitins.tp2.client.KeycloakUsuario;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

// Operacoes de administracao de usuarios no Keycloak, feitas com o token da service account
// do client fincontrol-backend (que so tem permissao de gerenciar usuarios do realm).
@ApplicationScoped
public class KeycloakAdminService {

    private static final Logger LOG = Logger.getLogger(KeycloakAdminService.class);

    private static final int VALIDADE_DO_LINK_EM_SEGUNDOS = 30 * 60;

    @Inject
    @RestClient
    KeycloakAdminClient adminClient;

    @Inject
    KeycloakTokenService keycloakTokenService;

    @ConfigProperty(name = "fincontrol.keycloak.client-id")
    String clientId;

    // para onde o link do e-mail volta depois que a senha e trocada
    @ConfigProperty(name = "fincontrol.frontend.url")
    String urlDoFront;

    // cria o usuario ja com a senha definitiva e devolve o id dele no Keycloak
    public String criarUsuario(String email, String nome, String senha) {
        String[] nomes = separarNome(nome);
        KeycloakUsuario novo = new KeycloakUsuario(null, email, email, nomes[0], nomes[1], true, true,
                List.of(KeycloakUsuario.Credencial.senha(senha)));

        try (Response resposta = executar(token -> adminClient.criarUsuario(token, novo))) {
            // Location: .../admin/realms/{realm}/users/{id}
            String location = resposta.getLocation().getPath();
            return location.substring(location.lastIndexOf('/') + 1);
        }
    }

    // e-mail (que tambem e o username, usado no login) e nome; o resto da conta nao muda.
    // Exige editUsernameAllowed no realm.
    public void atualizarDados(String keycloakId, String email, String nome) {
        String[] nomes = separarNome(nome);
        executar(token -> {
            adminClient.atualizarUsuario(token, keycloakId,
                    new KeycloakUsuario(null, email, email, nomes[0], nomes[1], null, null, null));
            return null;
        });
    }

    // o Keycloak 26 exige nome e sobrenome; sem eles o login responde "Account is not fully set up"
    private static String[] separarNome(String nome) {
        String[] partes = nome.trim().split("\\s+", 2);
        return new String[] { partes[0], partes.length > 1 ? partes[1] : partes[0] };
    }

    public void atribuirRole(String keycloakId, String nomeRole) {
        executar(token -> {
            KeycloakRole role = adminClient.buscarRole(token, nomeRole);
            adminClient.atribuirRoles(token, keycloakId, List.of(role));
            return null;
        });
    }

    // usuario desativado nao consegue mais logar
    public void definirAtivo(String keycloakId, boolean ativo) {
        executar(token -> {
            adminClient.atualizarUsuario(token, keycloakId, KeycloakUsuario.soAtivo(ativo));
            return null;
        });
    }

    // Encerra as sessoes e devolve os ids delas (claim "sid"): o refresh token deixa de valer e o
    // access token em uso e barrado pelo ContaAtivaAugmentor (por sid, sem depender de relogio)
    public List<String> encerrarSessoes(String keycloakId) {
        return executar(token -> {
            List<String> sessoes = adminClient.buscarSessoes(token, keycloakId).stream()
                    .map(KeycloakSessao::id)
                    .toList();
            adminClient.encerrarSessoes(token, keycloakId);
            return sessoes;
        });
    }

    // "Esqueci minha senha". Conta inexistente ou desativada nao recebe nada e nao gera erro, e a
    // falha no envio so vai para o log: a resposta nao pode revelar quais e-mails tem conta
    public void enviarRedefinicaoDeSenha(String email) {
        executar(token -> adminClient.buscarPorEmail(token, email, true)).stream()
                .filter(usuario -> Boolean.TRUE.equals(usuario.enabled()))
                .findFirst()
                .ifPresent(usuario -> {
                    try {
                        executar(token -> {
                            adminClient.enviarEmailDeAcoes(token, usuario.id(), clientId, urlDoFront + "/login",
                                    VALIDADE_DO_LINK_EM_SEGUNDOS, List.of("UPDATE_PASSWORD"));
                            return null;
                        });
                    } catch (ServicoExternoException | ResourceNotFoundException e) {
                        LOG.errorf(e, "Não foi possível enviar o e-mail de nova senha do usuário %s.", usuario.id());
                    }
                });
    }

    // melhor esforco: usado para desfazer um cadastro que falhou no meio
    public void excluirUsuario(String keycloakId) {
        try {
            executar(token -> {
                adminClient.excluirUsuario(token, keycloakId);
                return null;
            });
        } catch (RuntimeException e) {
            LOG.errorf(e, "Não foi possível excluir o usuário %s do Keycloak; remova-o manualmente.", keycloakId);
        }
    }

    // Executa a chamada com o token de admin. Se o token em cache foi recusado (401),
    // busca outro e tenta mais uma vez. 409 = e-mail ja cadastrado no Keycloak.
    private <T> T executar(Function<String, T> chamada) {
        try {
            return chamarComToken(chamada);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() != 401)
                throw traduzirErro(e);

            keycloakTokenService.descartarTokenDeAdmin();
            try {
                return chamarComToken(chamada);
            } catch (WebApplicationException novaFalha) {
                throw traduzirErro(novaFalha);
            }
        } catch (ValidationException | ServicoExternoException | ResourceNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.warnf(e, "Não foi possível conectar à Admin API do Keycloak");
            throw new ServicoExternoException("O serviço de contas está indisponível no momento.", e);
        }
    }

    private <T> T chamarComToken(Function<String, T> chamada) {
        return chamada.apply("Bearer " + keycloakTokenService.tokenDeAdmin());
    }

    private RuntimeException traduzirErro(WebApplicationException e) {
        int status = e.getResponse().getStatus();
        if (status == 409)
            return ValidationException.of("email", "Já existe uma conta com esse e-mail.");
        // ex.: usuario excluido direto no console do Keycloak
        if (status == 404)
            return new ResourceNotFoundException("Usuário não encontrado no Keycloak.");

        KeycloakErro erro = KeycloakTokenService.lerErro(e);
        LOG.errorf("Admin API do Keycloak respondeu HTTP %d (%s). Confira as roles da service account.",
                status, erro.errorMessage() != null ? erro.errorMessage() : erro.error());
        return new ServicoExternoException("O serviço de contas está indisponível no momento.", e);
    }
}
