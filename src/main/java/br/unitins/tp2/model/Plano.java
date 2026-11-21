package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

// Plano de assinatura do proprio FinControl: preco e os limites/modulos que libera ao usuario (RN14)
@Entity
public class Plano extends DefaultEntity {

    // plano que toda conta nova recebe (RN13) e para onde vai quem cancela um plano pago (RN16)
    public static final String NOME_GRATUITO = "Gratuito";

    private static final BigDecimal CEM = new BigDecimal("100");

    @Column(length = 60, nullable = false, unique = true)
    private String nome;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precoMensal;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precoAnual;

    @Column(nullable = false)
    private Integer limiteTransacoes;

    @Column(nullable = false)
    private Integer limiteMetas;

    @Column(nullable = false)
    private Boolean liberaRecorrencias;

    @Column(nullable = false)
    private Boolean liberaFiscal;

    @Column(nullable = false)
    private Boolean liberaExportacao;

    @Column(nullable = false)
    private Boolean ativo;

    // imagem do catalogo (uma por plano), guardada no S3; nula quando o plano nao tem imagem
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "chave", column = @Column(name = "imagemChave", length = 200)),
        @AttributeOverride(name = "nome", column = @Column(name = "imagemNome", length = 120)),
        @AttributeOverride(name = "tipo", column = @Column(name = "imagemTipo", length = 30)),
        @AttributeOverride(name = "tamanho", column = @Column(name = "imagemTamanho")),
        @AttributeOverride(name = "enviadoEm", column = @Column(name = "imagemEnviadaEm"))
    })
    private ArquivoArmazenado imagem;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoMensal() {
        return precoMensal;
    }

    public void setPrecoMensal(BigDecimal precoMensal) {
        this.precoMensal = precoMensal;
    }

    public BigDecimal getPrecoAnual() {
        return precoAnual;
    }

    public void setPrecoAnual(BigDecimal precoAnual) {
        this.precoAnual = precoAnual;
    }

    public Integer getLimiteTransacoes() {
        return limiteTransacoes;
    }

    public void setLimiteTransacoes(Integer limiteTransacoes) {
        this.limiteTransacoes = limiteTransacoes;
    }

    public Integer getLimiteMetas() {
        return limiteMetas;
    }

    public void setLimiteMetas(Integer limiteMetas) {
        this.limiteMetas = limiteMetas;
    }

    public Boolean getLiberaRecorrencias() {
        return liberaRecorrencias;
    }

    public void setLiberaRecorrencias(Boolean liberaRecorrencias) {
        this.liberaRecorrencias = liberaRecorrencias;
    }

    public Boolean getLiberaFiscal() {
        return liberaFiscal;
    }

    public void setLiberaFiscal(Boolean liberaFiscal) {
        this.liberaFiscal = liberaFiscal;
    }

    public Boolean getLiberaExportacao() {
        return liberaExportacao;
    }

    public void setLiberaExportacao(Boolean liberaExportacao) {
        this.liberaExportacao = liberaExportacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public ArquivoArmazenado getImagem() {
        return imagem;
    }

    public void setImagem(ArquivoArmazenado imagem) {
        this.imagem = imagem;
    }

    // preco de uma cobranca na periodicidade, menos o percentual do cupom; arredondado em centavos
    public BigDecimal calcularValor(Periodicidade periodicidade, Cupom cupom) {
        BigDecimal preco = periodicidade == Periodicidade.ANUAL ? precoAnual : precoMensal;
        if (cupom == null)
            return preco.setScale(2, RoundingMode.HALF_UP);

        BigDecimal fatorPago = CEM.subtract(cupom.getPercentualDesconto()).divide(CEM);
        return preco.multiply(fatorPago).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isGratuito() {
        return NOME_GRATUITO.equals(nome);
    }
}
