package br.unitins.tp2.dto;

import br.unitins.tp2.model.AtividadeMei;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.PerfilFiscal;

// senhaHash nunca sai da API
public record UsuarioResponseDTO(
    Long id,
    String nome,
    String email,
    String documento,
    String telefone,
    PerfilFiscal perfilFiscal,
    AtividadeMei atividadeMei,
    Papel papel,
    Boolean ativo,
    EnderecoResponseDTO endereco,
    // false no cadastro simples (sem dados fiscais e endereco)
    boolean cadastroCompleto,
    // so na listagem/consulta do admin; null quando nao se aplica (ou o usuario nao tem assinatura)
    AssinaturaResumoDTO assinatura
) {
}
