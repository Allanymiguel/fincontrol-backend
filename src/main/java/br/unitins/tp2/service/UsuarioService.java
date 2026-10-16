package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.model.Usuario;

// Gestao de usuarios pelo admin: so consulta e ativacao.
// O cadastro (e a senha) vira do Keycloak.
public interface UsuarioService {

    Usuario alternarAtivo(long id);
    Usuario findById(long id);
    List<Usuario> findAll(int page, int pageSize);
    List<Usuario> findByNomeOuEmail(String busca, int page, int pageSize);
    long count();
    long count(String busca);
}
