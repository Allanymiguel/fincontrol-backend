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
    // busca = parte do nome ou do email; idPlano = plano vigente da assinatura (ambos opcionais)
    List<Usuario> findByFiltros(String busca, Long idPlano, int page, int pageSize);
    long count(String busca, Long idPlano);
}
