package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.AssinaturaResumoDTO;
import br.unitins.tp2.dto.UsuarioResponseDTO;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Usuario;

public final class UsuarioResponseMapper {

    private UsuarioResponseMapper() {
    }

    public static UsuarioResponseDTO toResponse(Usuario usuario) {
        return toResponse(usuario, null);
    }

    // com o resumo da assinatura (listagem e consulta do admin)
    public static UsuarioResponseDTO toResponse(Usuario usuario, Assinatura assinatura) {
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
                EnderecoResponseMapper.toResponse(usuario.getEndereco()),
                toResumo(assinatura));
    }

    private static AssinaturaResumoDTO toResumo(Assinatura assinatura) {
        if (assinatura == null)
            return null;

        return new AssinaturaResumoDTO(
                assinatura.getPlano().getId(),
                assinatura.getPlano().getNome(),
                assinatura.getPeriodicidade(),
                assinatura.getValor(),
                assinatura.getDataProximaCobranca(),
                assinatura.isTrocaAgendada() ? assinatura.getProximoPlano().getNome() : null);
    }
}
