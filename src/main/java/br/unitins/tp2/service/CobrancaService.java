package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.CobrancaResponseDTO;
import br.unitins.tp2.dto.ExecucaoCobrancaResponseDTO;
import br.unitins.tp2.model.ExecucaoCobranca;
import br.unitins.tp2.model.OrigemExecucaoCobranca;
import br.unitins.tp2.model.StatusExecucaoCobranca;

public interface CobrancaService {

    // Processa as cobrancas vencidas ate a data (inclusive); rodar de novo na mesma data nao cobra duas
    // vezes (so a cobranca recusada ganha nova tentativa).
    // Cada assinatura vai na propria transacao (uma falha nao desfaz as outras) e a execucao e
    // registrada no historico mesmo se falhar. admin = quem disparou (nulo na automatica).
    CobrancaResponseDTO executar(LocalDate data, OrigemExecucaoCobranca origem, AuditoriaService.Admin admin);

    List<ExecucaoCobranca> buscarExecucoes(LocalDate inicio, LocalDate fim, OrigemExecucaoCobranca origem,
            StatusExecucaoCobranca status, int page, int pageSize);

    long contarExecucoes(LocalDate inicio, LocalDate fim, OrigemExecucaoCobranca origem, StatusExecucaoCobranca status);

    ExecucaoCobranca findExecucao(long id);

    // dias do periodo (ate ontem) sem nenhuma execucao automatica
    List<LocalDate> diasSemAutomatica(LocalDate inicio, LocalDate fim);

    // "a cobranca de ontem rodou direito?" e o que chamou atencao nos ultimos dias
    ExecucaoCobrancaResponseDTO.Resumo resumo(int dias);
}
