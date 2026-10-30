package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Faixa de CEP de uma UF (tabela dos Correios, em referencia.sql): identifica o estado pelos
// primeiros digitos do CEP, sem consulta externa. Algumas UFs tem mais de uma faixa (AM, DF, GO).
@Entity
public class FaixaCepUf extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    // 8 digitos; como o CEP tem tamanho fixo, a comparacao de texto funciona
    @Column(length = 8, nullable = false)
    private String cepInicial;

    @Column(length = 8, nullable = false)
    private String cepFinal;

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getCepInicial() {
        return cepInicial;
    }

    public void setCepInicial(String cepInicial) {
        this.cepInicial = cepInicial;
    }

    public String getCepFinal() {
        return cepFinal;
    }

    public void setCepFinal(String cepFinal) {
        this.cepFinal = cepFinal;
    }

    public boolean contem(String cep) {
        return cep.compareTo(cepInicial) >= 0 && cep.compareTo(cepFinal) <= 0;
    }
}
