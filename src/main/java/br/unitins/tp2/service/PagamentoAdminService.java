package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.EstornoPagamentoDTO;
import br.unitins.tp2.dto.PagamentoResponseDTO;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.StatusPagamento;
import jakarta.validation.Valid;

// Todos os pagamentos das assinaturas (relacao do usuario com o FinControl), para o admin
public interface PagamentoAdminService {

    // filtros opcionais; nulo = nao filtra. Periodo pela data do pagamento.
    record Filtro(LocalDate inicio, LocalDate fim, Long idPlano, FormaPagamento forma, StatusPagamento status,
            String busca) {
    }

    List<Pagamento> buscar(Filtro filtro, int page, int pageSize);

    long contar(Filtro filtro);

    // quantidade e soma por status com os mesmos filtros (menos o de status)
    List<PagamentoResponseDTO.TotalPorStatus> totais(Filtro filtro);

    Pagamento findById(long id);

    // so o registro financeiro: a assinatura nao muda (o periodo pago continua valendo)
    Pagamento estornar(long id, @Valid EstornoPagamentoDTO estorno);
}
