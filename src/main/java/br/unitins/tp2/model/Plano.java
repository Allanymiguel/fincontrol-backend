package br.unitins.tp2.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

// Plano de assinatura do proprio FinControl: preco e os limites/modulos que libera ao usuario (RN14)
@Entity
public class Plano extends DefaultEntity {

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
}
