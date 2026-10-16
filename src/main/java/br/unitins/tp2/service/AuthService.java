package br.unitins.tp2.service;

import br.unitins.tp2.dto.LoginRequestDTO;
import br.unitins.tp2.dto.RefreshTokenRequestDTO;
import br.unitins.tp2.dto.TokenResponseDTO;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;

// Login pelo back: repassa e-mail/senha ao Keycloak e devolve o token que ele emitiu
public interface AuthService {

    TokenResponseDTO login(@Valid LoginRequestDTO login);
    TokenResponseDTO renovar(@Valid RefreshTokenRequestDTO refresh);
    void logout(@Valid RefreshTokenRequestDTO refresh);
    Usuario usuarioLogado(String keycloakId);
}
