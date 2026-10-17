package br.unitins.tp2.service;

import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Usuario;

public interface AssinaturaService {

    // RN13: assinatura inicial de toda conta nova, no plano Gratuito
    Assinatura criarGratuita(Usuario usuario);
    Assinatura buscarDoUsuario(long idUsuario);
}
