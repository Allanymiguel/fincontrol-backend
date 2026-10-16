package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.model.Usuario;

public final class UsuarioResponseMapper {

    private UsuarioResponseMapper() {
    }

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getDocumento(),
                usuario.getTelefone(),
                usuario.getPerfilFiscal(),
                usuario.getAtividadeMei(),
                usuario.getPapel(),
                usuario.getAtivo(),
                EnderecoResponseMapper.toResponse(usuario.getEndereco()));
    }
}
