package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// Uma assinatura por usuario (RN16): ela nasce no Gratuito e depois so muda de plano.
@ApplicationScoped
public class AssinaturaServiceImpl implements AssinaturaService {

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PlanoRepository planoRepository;

    // Sem o plano Gratuito configurado, o cadastro falha (e o usuario do Keycloak e desfeito
    // pelo UsuarioServiceImpl.registrar).
    @Override
    @Transactional
    public Assinatura criarGratuita(Usuario usuario) {
        Plano gratuito = buscarGratuitoOuFalhar();
        LocalDate hoje = LocalDate.now();

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setStatus(StatusAssinatura.ATIVA);
        assinatura.setDataInicio(hoje);
        assinatura.setObservacao("Plano gratuito aplicado no cadastro");
        aplicarGratuito(assinatura, gratuito, hoje);
        assinaturaRepository.persist(assinatura);

        return assinatura;
    }

    @Override
    public Assinatura buscarDoUsuario(long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario);
        if (usuario == null)
            throw new ResourceNotFoundException("Usuário não encontrado.");

        Assinatura assinatura = assinaturaRepository.findByUsuario(idUsuario);
        if (assinatura == null)
            throw new ResourceNotFoundException("Este usuário não tem assinatura.");

        return assinatura;
    }

    // plano Gratuito: sem cobranca, sem cupom e sem forma de pagamento
    static void aplicarGratuito(Assinatura assinatura, Plano gratuito, LocalDate data) {
        assinatura.setPlano(gratuito);
        assinatura.setPeriodicidade(Periodicidade.MENSAL);
        assinatura.setCupom(null);
        assinatura.setFormaPagamento(FormaPagamento.NENHUMA);
        assinatura.setValor(BigDecimal.ZERO.setScale(2));
        assinatura.setDataInicioPeriodo(data);
        assinatura.setDataProximaCobranca(null);
        assinatura.setValorPeriodo(BigDecimal.ZERO.setScale(2));
        assinatura.limparTrocaAgendada();
    }

    Plano buscarGratuitoOuFalhar() {
        Plano gratuito = planoRepository.findByNomeExato(Plano.NOME_GRATUITO);
        if (gratuito == null)
            throw ValidationException.of("idPlano",
                    "Plano \"" + Plano.NOME_GRATUITO + "\" não encontrado. Configure-o antes de continuar.");

        return gratuito;
    }
}
