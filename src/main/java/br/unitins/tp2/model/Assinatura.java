package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

// O contrato do Usuario com o FinControl: uma unica assinatura por usuario (RN16).
// Trocar de plano altera esta mesma assinatura; o historico do que foi cobrado fica em Pagamento.
@Entity
public class Assinatura extends DefaultEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    // plano vigente: e ele que libera os limites e modulos (RN14)
    @ManyToOne(optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    // opcional: cupom aplicado ao plano vigente (vale em todos os ciclos dele)
    @ManyToOne
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    @Column(nullable = false)
    private Periodicidade periodicidade;

    // forma usada nas cobrancas; NENHUMA no plano gratuito
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private StatusAssinatura status;

    // quando o usuario passou a ter a assinatura (cadastro)
    @Column(nullable = false)
    private LocalDate dataInicio;

    // preco de cada cobranca do plano vigente, ja com o cupom
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    // periodo atual: de dataInicioPeriodo ate dataProximaCobranca, coberto por valorPeriodo.
    // No plano gratuito nao ha cobranca: dataProximaCobranca fica nula.
    @Column(nullable = false)
    private LocalDate dataInicioPeriodo;

    private LocalDate dataProximaCobranca;

    // quanto foi pago (ou creditado, numa troca imediata) pelo periodo atual
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorPeriodo;

    // troca agendada para a proxima cobranca (plano mais barato ou cancelamento); nulos = nada agendado
    @ManyToOne
    @JoinColumn(name = "proximo_plano_id")
    private Plano proximoPlano;

    private Periodicidade proximaPeriodicidade;

    @ManyToOne
    @JoinColumn(name = "proximo_cupom_id")
    private Cupom proximoCupom;

    private FormaPagamento proximaFormaPagamento;

    @Column(length = 255)
    private String observacao;

    public boolean isTrocaAgendada() {
        return proximoPlano != null;
    }

    public void limparTrocaAgendada() {
        proximoPlano = null;
        proximaPeriodicidade = null;
        proximoCupom = null;
        proximaFormaPagamento = null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public Cupom getCupom() {
        return cupom;
    }

    public void setCupom(Cupom cupom) {
        this.cupom = cupom;
    }

    public Periodicidade getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(Periodicidade periodicidade) {
        this.periodicidade = periodicidade;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusAssinatura getStatus() {
        return status;
    }

    public void setStatus(StatusAssinatura status) {
        this.status = status;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataInicioPeriodo() {
        return dataInicioPeriodo;
    }

    public void setDataInicioPeriodo(LocalDate dataInicioPeriodo) {
        this.dataInicioPeriodo = dataInicioPeriodo;
    }

    public LocalDate getDataProximaCobranca() {
        return dataProximaCobranca;
    }

    public void setDataProximaCobranca(LocalDate dataProximaCobranca) {
        this.dataProximaCobranca = dataProximaCobranca;
    }

    public BigDecimal getValorPeriodo() {
        return valorPeriodo;
    }

    public void setValorPeriodo(BigDecimal valorPeriodo) {
        this.valorPeriodo = valorPeriodo;
    }

    public Plano getProximoPlano() {
        return proximoPlano;
    }

    public void setProximoPlano(Plano proximoPlano) {
        this.proximoPlano = proximoPlano;
    }

    public Periodicidade getProximaPeriodicidade() {
        return proximaPeriodicidade;
    }

    public void setProximaPeriodicidade(Periodicidade proximaPeriodicidade) {
        this.proximaPeriodicidade = proximaPeriodicidade;
    }

    public Cupom getProximoCupom() {
        return proximoCupom;
    }

    public void setProximoCupom(Cupom proximoCupom) {
        this.proximoCupom = proximoCupom;
    }

    public FormaPagamento getProximaFormaPagamento() {
        return proximaFormaPagamento;
    }

    public void setProximaFormaPagamento(FormaPagamento proximaFormaPagamento) {
        this.proximaFormaPagamento = proximaFormaPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
