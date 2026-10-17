package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.RegistroRequestDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;

// Cadastro da conta (Keycloak + perfil local) e gestao de usuarios pelo admin
public interface UsuarioService {

    Usuario registrar(@Valid RegistroRequestDTO registro);
    Usuario alternarAtivo(long id, String keycloakIdDoAdmin);
    Usuario findById(long id);
    UsuarioResumoResponseDTO buscarResumo(long id);
    List<Usuario> findAll(int page, int pageSize);
    List<Usuario> findByNomeOuEmail(String busca, int page, int pageSize);
    long count();
    long count(String busca);
}
