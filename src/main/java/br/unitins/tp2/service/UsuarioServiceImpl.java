package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UsuarioServiceImpl implements UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    // ativa se estiver inativo e desativa se estiver ativo
    // TODO: com o Keycloak ligado, impedir que o admin desative a propria conta
    // e refletir o bloqueio no Keycloak (usuario inativo nao deve conseguir logar)
    @Override
    @Transactional
    public Usuario alternarAtivo(long id) {
        Usuario usuario = buscarUsuarioOuFalhar(id);
        usuario.setAtivo(!usuario.getAtivo());

        return usuario;
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

    private Usuario buscarUsuarioOuFalhar(long id) {
        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null)
            throw new ResourceNotFoundException("Usuário não encontrado.");

        return usuario;
    }
}
