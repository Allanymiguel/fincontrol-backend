package br.unitins.tp2.dto;

import java.time.LocalDateTime;

// Um acontecimento na relacao do usuario com o FinControl (conta, assinatura, pagamento, acao do
// admin). Nada das financas pessoais, documento ou telefone.
// categoria: "conta", "assinatura", "pagamento" ou "admin"
public record EventoLinhaDoTempoDTO(LocalDateTime dataHora, String categoria, String titulo, String descricao) {
}
