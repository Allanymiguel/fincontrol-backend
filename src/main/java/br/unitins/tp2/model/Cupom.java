package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

// Cupom de desconto aplicado na assinatura de um plano
@Entity
public class Cupom extends DefaultEntity {

    // salvo sempre em maiusculas, para "bemvindo10" e "BEMVINDO10" serem o mesmo cupom
    @Column(length = 30, nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    // ultimo dia em que o cupom pode ser usado
    @Column(nullable = false)
    private LocalDate validade;

    @Column(nullable = false)
    private Boolean ativo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
