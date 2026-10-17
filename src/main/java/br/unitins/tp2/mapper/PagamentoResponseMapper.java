package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.PagamentoResponseDTO;
import br.unitins.tp2.model.Pagamento;

public final class PagamentoResponseMapper {

    private PagamentoResponseMapper() {
    }

    public static PagamentoResponseDTO toResponse(Pagamento pagamento) {
        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getDataPagamento(),
                pagamento.getPlano().getNome(),
                pagamento.getPeriodicidade(),
                pagamento.getCupom() == null ? null : pagamento.getCupom().getCodigo(),
                pagamento.getValor(),
                pagamento.getFormaPagamento(),
                pagamento.getStatus());
    }
}
