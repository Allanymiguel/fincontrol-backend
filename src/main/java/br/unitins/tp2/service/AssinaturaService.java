package br.unitins.tp2.service;

import br.unitins.tp2.dto.ObservacaoAssinaturaDTO;
import br.unitins.tp2.dto.TrocaPlanoDTO;
import br.unitins.tp2.dto.TrocaPlanoResponseDTO;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;

public interface AssinaturaService {

    // RN13: assinatura inicial de toda conta nova, no plano Gratuito
    Assinatura criarGratuita(Usuario usuario);
    Assinatura buscarDoUsuario(long idUsuario);

    // RN16: troca no estilo da Netflix (mais caro vale na hora, mais barato na proxima cobranca)
    TrocaPlanoResponseDTO simularTroca(long idUsuario, @Valid TrocaPlanoDTO troca);
    TrocaPlanoResponseDTO trocarPlano(long idUsuario, @Valid TrocaPlanoDTO troca);
    // agenda a volta ao Gratuito na proxima cobranca
    TrocaPlanoResponseDTO cancelarPlanoPago(long idUsuario);
    Assinatura desfazerTroca(long idUsuario);
    Assinatura salvarObservacao(long idUsuario, @Valid ObservacaoAssinaturaDTO observacao);
}
