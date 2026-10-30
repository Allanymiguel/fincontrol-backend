package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.PagamentoResponseDTO;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.Usuario;

public final class PagamentoResponseMapper {

    private PagamentoResponseMapper() {
    }

    public static PagamentoResponseDTO toResponse(Pagamento pagamento) {
        Usuario usuario = pagamento.getAssinatura().getUsuario();
        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getDataPagamento(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                pagamento.getPlano().getId(),
                pagamento.getPlano().getNome(),
                pagamento.getPeriodicidade(),
                pagamento.getCupom() == null ? null : pagamento.getCupom().getId(),
                pagamento.getCupom() == null ? null : pagamento.getCupom().getCodigo(),
                pagamento.getValor(),
                pagamento.getValorDesconto(),
                pagamento.getFormaPagamento(),
                pagamento.getStatus(),
                pagamento.getDataEstorno(),
                pagamento.getMotivoEstorno(),
                pagamento.getEstornadoPor());
    }
}
