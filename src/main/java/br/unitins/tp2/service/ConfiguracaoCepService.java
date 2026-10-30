package br.unitins.tp2.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

// Interruptor da consulta externa (ViaCEP) do endereco de clientes. Comeca com o valor de
// fincontrol.cep.api-externa.habilitada e o admin muda em tempo de execucao, sem reiniciar o back
// (ao reiniciar, volta ao valor da propriedade). Com ele desligado o cadastro usa so a base local
// e o modo manual.
@ApplicationScoped
public class ConfiguracaoCepService {

    @ConfigProperty(name = "fincontrol.cep.api-externa.habilitada", defaultValue = "true")
    boolean valorInicial;

    private final AtomicBoolean apiExternaHabilitada = new AtomicBoolean(true);

    @PostConstruct
    void iniciar() {
        apiExternaHabilitada.set(valorInicial);
    }

    public boolean isApiExternaHabilitada() {
        return apiExternaHabilitada.get();
    }

    public void definirApiExternaHabilitada(boolean habilitada) {
        apiExternaHabilitada.set(habilitada);
    }
}
