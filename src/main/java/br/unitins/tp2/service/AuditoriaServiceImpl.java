package br.unitins.tp2.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;

import br.unitins.tp2.dto.AlteracaoAuditoriaDTO;
import br.unitins.tp2.dto.FiltrosAuditoriaResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.EventoAuditoria;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.EventoAuditoriaRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.AlteracoesAuditoria;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final String SEM_LOGIN = "Sem login (modo de desenvolvimento)";

    @Inject
    EventoAuditoriaRepository eventoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    @Override
    @Transactional
    public void registrar(AcaoAuditoria acao, TipoAlvoAuditoria tipoAlvo, Long idAlvo, String descricaoAlvo,
            Map<String, String> antes, Map<String, String> depois) {
        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(antes, depois);
        boolean edicao = !antes.isEmpty() && !depois.isEmpty();
        if (edicao && alteracoes.isEmpty())
            return;

        EventoAuditoria evento = new EventoAuditoria();
        evento.setDataHora(LocalDateTime.now());
        preencherAdmin(evento);
        evento.setAcao(acao);
        evento.setTipoAlvo(tipoAlvo);
        evento.setIdAlvo(idAlvo);
        evento.setDescricaoAlvo(limitar(descricaoAlvo, 160));
        evento.setAlteracoes(AlteracoesAuditoria.paraJson(alteracoes));
        eventoRepository.persist(evento);
    }

    @Override
    public boolean isAdminLogado() {
        return !identity.isAnonymous() && identity.hasRole("ADMIN");
    }

    // admin = dono do token (sub); nome e e-mail vem do perfil local, ou das claims do token
    private void preencherAdmin(EventoAuditoria evento) {
        String keycloakId = identity.isAnonymous() ? null : jwt.getSubject();
        if (keycloakId == null || keycloakId.isBlank()) {
            evento.setAdminNome(SEM_LOGIN);
            return;
        }
        evento.setAdminKeycloakId(keycloakId);
        Usuario admin = usuarioRepository.findByKeycloakId(keycloakId);
        String nome = admin != null ? admin.getNome() : jwt.getClaim("name");
        String email = admin != null ? admin.getEmail() : jwt.getClaim("email");
        evento.setAdminNome(nome != null ? nome : "Administrador");
        evento.setAdminEmail(email);
    }

    @Override
    public List<EventoAuditoria> buscar(Filtro filtro, int page, int pageSize) {
        return consulta(filtro).page(page, pageSize).list();
    }

    @Override
    public long contar(Filtro filtro) {
        return consulta(filtro).count();
    }

    private PanacheQuery<EventoAuditoria> consulta(Filtro filtro) {
        if (filtro.inicio() != null && filtro.fim() != null && filtro.inicio().isAfter(filtro.fim()))
            throw ValidationException.of("inicio", "A data inicial deve ser anterior ou igual à final.");
        return eventoRepository.findByFiltros(filtro.idAdmin(), filtro.acao(), filtro.tipoAlvo(), filtro.idAlvo(),
                filtro.busca(),
                filtro.inicio() == null ? null : filtro.inicio().atStartOfDay(),
                filtro.fim() == null ? null : filtro.fim().plusDays(1).atStartOfDay());
    }

    @Override
    public EventoAuditoria findById(long id) {
        EventoAuditoria evento = eventoRepository.findById(id);
        if (evento == null)
            throw new ResourceNotFoundException("Evento de auditoria não encontrado.");
        return evento;
    }

    @Override
    public FiltrosAuditoriaResponseDTO filtros() {
        List<FiltrosAuditoriaResponseDTO.Admin> admins = eventoRepository.findAdmins().stream()
                .map(linha -> new FiltrosAuditoriaResponseDTO.Admin((String) linha[0], (String) linha[1],
                        (String) linha[2]))
                .toList();
        return new FiltrosAuditoriaResponseDTO(Arrays.asList(AcaoAuditoria.values()),
                Arrays.asList(TipoAlvoAuditoria.values()), admins);
    }

    private static String limitar(String texto, int tamanho) {
        if (texto == null)
            return null;
        return texto.length() <= tamanho ? texto : texto.substring(0, tamanho);
    }
}
