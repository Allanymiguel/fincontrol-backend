package br.unitins.tp2.service;

import java.time.LocalDate;

import br.unitins.tp2.dto.CobrancaResponseDTO;

public interface CobrancaService {

    // processa as cobrancas vencidas ate a data (inclusive); rodar de novo na mesma data nao duplica
    CobrancaResponseDTO processar(LocalDate data);
}
