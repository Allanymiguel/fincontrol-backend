package br.unitins.tp2.dto;

import java.time.LocalDate;
import java.util.List;

// Visao do admin sobre um usuario, sem invadir os dados pessoais: perfil, assinaturas e
// apenas QUANTIDADES dos registros operacionais (nunca o conteudo de transacoes/contatos/categorias).
public record UsuarioResumoResponseDTO(
    UsuarioResponseDTO usuario,
    // null se o usuario nao tiver assinatura ATIVA
    AssinaturaResponseDTO assinaturaAtual,
    // todas as assinaturas, da mais recente para a mais antiga
    List<AssinaturaResponseDTO> assinaturas,
    Contagens contagens,
    // data da transacao mais recente; null se nao tiver nenhuma
    LocalDate ultimaTransacaoEm
) {

    public record Contagens(long transacoes, long categorias, long contatos) {
    }
}
