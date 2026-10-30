package br.unitins.tp2.service;

import java.time.LocalDate;

import br.unitins.tp2.dto.DashboardResponseDTO;

// Indicadores do painel do admin (so assinatura, plano e pagamento; nada das financas pessoais)
public interface DashboardService {

    // inicio e fim opcionais: sem eles, os ultimos 6 meses ate hoje
    DashboardResponseDTO gerar(LocalDate inicio, LocalDate fim);
}
