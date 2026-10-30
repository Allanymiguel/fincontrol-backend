package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Uma cobranca da Assinatura (simulada, RN15: sem gateway, ja nasce APROVADO).
// Plano, periodicidade e cupom sao copiados do momento da cobranca, para o historico continuar
// legivel depois que a assinatura trocar de plano.
@Entity
public class Pagamento extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "assinatura_id", nullable = false)
    private Assinatura assinatura;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private Plano plano;

    @Column(nullable = false)
    private Periodicidade periodicidade;

    @ManyToOne
    @JoinColumn(name = "cupom_id")
    private Cupom cupom;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    // quanto o cupom tirou do preco cheio nesta cobranca (zero sem cupom). Guardado no momento da
    // cobranca: editar o percentual ou o preco depois nao muda o desconto ja concedido
    @Column(nullable = false, precision = 14, scale = 2)
    @ColumnDefault("0")
    private BigDecimal valorDesconto = BigDecimal.ZERO.setScale(2);

    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false)
    private LocalDate dataPagamento;

    // estorno pelo admin: quando, por que e quem (copiado do token); nulos se nao foi estornado
    private LocalDateTime dataEstorno;

    @Column(length = 255)
    private String motivoEstorno;

    @Column(length = 160)
    private String estornadoPor;

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public LocalDateTime getDataEstorno() {
        return dataEstorno;
    }

    public void setDataEstorno(LocalDateTime dataEstorno) {
        this.dataEstorno = dataEstorno;
    }

    public String getMotivoEstorno() {
        return motivoEstorno;
    }

    public void setMotivoEstorno(String motivoEstorno) {
        this.motivoEstorno = motivoEstorno;
    }

    public String getEstornadoPor() {
        return estornadoPor;
    }

    public void setEstornadoPor(String estornadoPor) {
        this.estornadoPor = estornadoPor;
    }

    public Assinatura getAssinatura() {
        return assinatura;
    }

    public void setAssinatura(Assinatura assinatura) {
        this.assinatura = assinatura;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public Periodicidade getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(Periodicidade periodicidade) {
        this.periodicidade = periodicidade;
    }

    public Cupom getCupom() {
        return cupom;
    }

    public void setCupom(Cupom cupom) {
        this.cupom = cupom;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
