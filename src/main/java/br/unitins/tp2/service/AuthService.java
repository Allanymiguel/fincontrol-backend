package br.unitins.tp2.service;

import br.unitins.tp2.client.KeycloakTokenResponse;
import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

// Login pelo back: repassa e-mail/senha ao Keycloak e devolve os tokens que ele emitiu
public interface AuthService {

    KeycloakTokenResponse login(@NotNull(message = "Informe o e-mail e a senha.") @Valid LoginRequestDTO login);

    KeycloakTokenResponse renovar(String refreshToken);

    void logout(String refreshToken);

    Usuario usuarioLogado(String keycloakId);
}
