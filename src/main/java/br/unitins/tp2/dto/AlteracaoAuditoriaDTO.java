package br.unitins.tp2.dto;

// Um campo que mudou numa acao do admin. oculto = dado sensivel (documento, telefone): so se
// registra que mudou, sem os valores (antes e depois ficam nulos).
public record AlteracaoAuditoriaDTO(String campo, String antes, String depois, boolean oculto) {
}
