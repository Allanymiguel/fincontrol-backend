package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Uma faixa da tabela progressiva mensal do IRPF. As faixas com o mesmo inicio de vigencia
// formam uma tabela; o simulador (RN10) usa a tabela vigente na data do calculo.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "vigenciaInicio", "limiteInferior" }))
public class FaixaIrpf extends DefaultEntity {

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal limiteInferior;

    // nulo na ultima faixa ("acima de ...")
    @Column(precision = 14, scale = 2)
    private BigDecimal limiteSuperior;

    // percentual, de 0 a 100
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquota;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal parcelaDeduzir;

    @Column(nullable = false)
    private LocalDate vigenciaInicio;

    public BigDecimal getLimiteInferior() {
        return limiteInferior;
    }

    public void setLimiteInferior(BigDecimal limiteInferior) {
        this.limiteInferior = limiteInferior;
    }

    public BigDecimal getLimiteSuperior() {
        return limiteSuperior;
    }

    public void setLimiteSuperior(BigDecimal limiteSuperior) {
        this.limiteSuperior = limiteSuperior;
    }

    public BigDecimal getAliquota() {
        return aliquota;
    }

    public void setAliquota(BigDecimal aliquota) {
        this.aliquota = aliquota;
    }

    public BigDecimal getParcelaDeduzir() {
        return parcelaDeduzir;
    }

    public void setParcelaDeduzir(BigDecimal parcelaDeduzir) {
        this.parcelaDeduzir = parcelaDeduzir;
    }

    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }

    public void setVigenciaInicio(LocalDate vigenciaInicio) {
        this.vigenciaInicio = vigenciaInicio;
    }
}
