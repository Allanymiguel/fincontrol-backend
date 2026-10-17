package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.AssinaturaResponseDTO;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;

public final class AssinaturaResponseMapper {

    private AssinaturaResponseMapper() {
    }

    public static AssinaturaResponseDTO toResponse(Assinatura assinatura) {
        if (assinatura == null)
            return null;

        Cupom cupom = assinatura.getCupom();

        return new AssinaturaResponseDTO(
                assinatura.getId(),
                assinatura.getUsuario().getId(),
                assinatura.getUsuario().getNome(),
                assinatura.getUsuario().getEmail(),
                assinatura.getPlano().getId(),
                assinatura.getPlano().getNome(),
                assinatura.getPeriodicidade(),
                cupom == null ? null : cupom.getId(),
                cupom == null ? null : cupom.getCodigo(),
                cupom == null ? null : cupom.getPercentualDesconto(),
                assinatura.getFormaPagamento(),
                assinatura.getStatus(),
                assinatura.getDataInicio(),
                assinatura.getValor(),
                assinatura.getDataInicioPeriodo(),
                assinatura.getDataProximaCobranca(),
                toTrocaAgendada(assinatura),
                assinatura.getObservacao());
    }

    private static AssinaturaResponseDTO.TrocaAgendada toTrocaAgendada(Assinatura assinatura) {
        if (!assinatura.isTrocaAgendada())
            return null;

        Cupom cupom = assinatura.getProximoCupom();
        return new AssinaturaResponseDTO.TrocaAgendada(
                assinatura.getProximoPlano().getId(),
                assinatura.getProximoPlano().getNome(),
                assinatura.getProximaPeriodicidade(),
                cupom == null ? null : cupom.getId(),
                cupom == null ? null : cupom.getCodigo(),
                assinatura.getProximaFormaPagamento(),
                assinatura.getProximoPlano().calcularValor(assinatura.getProximaPeriodicidade(), cupom),
                assinatura.getDataProximaCobranca());
    }
}
