package br.unitins.tp2.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.unitins.tp2.model.OrigemExecucaoCobranca;
import br.unitins.tp2.model.StatusExecucaoCobranca;

public record ExecucaoCobrancaResponseDTO(
    Long id,
    LocalDate dataReferencia,
    LocalDateTime inicio,
    LocalDateTime fim,
    Long duracaoMs,
    OrigemExecucaoCobranca origem,
    StatusExecucaoCobranca status,
    String disparadoPor,
    int assinaturasProcessadas,
    int pagamentosGerados,
    BigDecimal valorTotal,
    int trocasAplicadas,
    int voltasAoGratuito,
    String mensagemErro,
    List<Erro> erros
) {

    // assinatura que falhou (as outras da execucao seguiram)
    public record Erro(Long idAssinatura, String usuario, String mensagem) {
    }

    // "a cobranca de ontem rodou direito?": a ultima automatica de ontem, falhas e dias sem execucao
    public record Resumo(LocalDate ontem, ExecucaoCobrancaResponseDTO automaticaDeOntem, long falhasNoPeriodo,
            List<LocalDate> diasSemAutomatica, LocalDate inicioPeriodo) {
    }
}
