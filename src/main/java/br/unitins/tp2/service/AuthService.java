package br.unitins.tp2.service;

import br.unitins.tp2.model.Usuario;

// O login e feito direto no Keycloak (pelo front); o back so valida o token e acha o perfil local
public interface AuthService {

    Usuario usuarioLogado(String keycloakId);
}
