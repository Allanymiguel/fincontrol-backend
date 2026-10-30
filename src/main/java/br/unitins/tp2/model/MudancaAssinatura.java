package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Historico da Assinatura, so de insercao (@Immutable: o Hibernate nunca gera UPDATE).
// A Assinatura guarda so o estado atual; cada mudanca (cadastro, upgrade, downgrade e
// cancelamento agendados ou aplicados, agendamento desfeito, volta ao Gratuito) vira uma linha
// aqui, com o plano de antes e o de depois. E daqui que saem a evolucao por plano e a linha do tempo.
@Entity
@Immutable
public class MudancaAssinatura extends DefaultEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "assinatura_id", nullable = false)
    private Assinatura assinatura;

    @Column(nullable = false)
    private TipoMudancaAssinatura tipo;

    @Column(nullable = false)
    private OrigemMudancaAssinatura origem;

    // dia em que a mudanca vale (na cobranca, o dia do vencimento)
    @Column(nullable = false)
    private LocalDate data;

    // plano de antes (nulo no cadastro). No agendamento desfeito, e o plano que estava agendado.
    @ManyToOne
    @JoinColumn(name = "plano_anterior_id")
    private Plano planoAnterior;

    private Periodicidade periodicidadeAnterior;

    // valores mensais equivalentes (anual / 12), ja com o cupom
    @Column(precision = 14, scale = 2)
    private BigDecimal valorMensalAnterior;

    // plano de depois (no agendamento, o plano agendado)
    @ManyToOne
    @JoinColumn(name = "plano_novo_id")
    private Plano planoNovo;

    private Periodicidade periodicidadeNova;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorMensalNovo;

    @Column(length = 255)
    private String detalhe;

    // plano, periodicidade e valor de cada cobranca; guarda o valor como equivalente mensal
    public void definirAnterior(Plano plano, Periodicidade periodicidade, BigDecimal valorCobranca) {
        this.planoAnterior = plano;
        this.periodicidadeAnterior = periodicidade;
        this.valorMensalAnterior = periodicidade.valorMensal(valorCobranca);
    }

    public void definirNovo(Plano plano, Periodicidade periodicidade, BigDecimal valorCobranca) {
        this.planoNovo = plano;
        this.periodicidadeNova = periodicidade;
        this.valorMensalNovo = periodicidade.valorMensal(valorCobranca);
    }

    public Assinatura getAssinatura() {
        return assinatura;
    }

    public void setAssinatura(Assinatura assinatura) {
        this.assinatura = assinatura;
    }

    public TipoMudancaAssinatura getTipo() {
        return tipo;
    }

    public void setTipo(TipoMudancaAssinatura tipo) {
        this.tipo = tipo;
    }

    public OrigemMudancaAssinatura getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemMudancaAssinatura origem) {
        this.origem = origem;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Plano getPlanoAnterior() {
        return planoAnterior;
    }

    public Periodicidade getPeriodicidadeAnterior() {
        return periodicidadeAnterior;
    }

    public BigDecimal getValorMensalAnterior() {
        return valorMensalAnterior;
    }

    public Plano getPlanoNovo() {
        return planoNovo;
    }

    public Periodicidade getPeriodicidadeNova() {
        return periodicidadeNova;
    }

    public BigDecimal getValorMensalNovo() {
        return valorMensalNovo;
    }

    public String getDetalhe() {
        return detalhe;
    }

    public void setDetalhe(String detalhe) {
        this.detalhe = detalhe;
    }
}
