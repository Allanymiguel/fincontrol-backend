package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.AssinaturaResponseDTO;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;

public final class AssinaturaResponseMapper {

    private AssinaturaResponseMapper() {
    }

    public static AssinaturaResponseDTO toResponse(Assinatura assinatura) {
        Cupom cupom = assinatura.getCupom();

        return new AssinaturaResponseDTO(
                assinatura.getId(),
                assinatura.getUsuario().getId(),
                assinatura.getUsuario().getNome(),
                assinatura.getUsuario().getEmail(),
                assinatura.getPlano().getId(),
                assinatura.getPlano().getNome(),
                cupom == null ? null : cupom.getId(),
                cupom == null ? null : cupom.getCodigo(),
                cupom == null ? null : cupom.getPercentualDesconto(),
                assinatura.getStatus(),
                assinatura.getDataInicio(),
                assinatura.getDataFim(),
                assinatura.getPeriodicidade(),
                assinatura.getValorPago(),
                assinatura.getFormaPagamento(),
                assinatura.getObservacao());
    }
}
