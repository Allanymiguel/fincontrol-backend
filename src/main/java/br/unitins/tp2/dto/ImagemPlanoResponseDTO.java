package br.unitins.tp2.dto;

import java.time.LocalDateTime;

// Metadados da imagem do plano. A chave do S3 fica na API: o front monta a URL com a versao
// (GET /planos/{id}/imagem?v={versao}), que muda a cada envio.
public record ImagemPlanoResponseDTO(
    String nome,
    String tipo,
    Long tamanho,
    LocalDateTime enviadaEm,
    String versao
) {
}
