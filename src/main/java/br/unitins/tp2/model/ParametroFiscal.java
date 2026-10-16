package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

// Valor usado nos calculos fiscais (salario minimo, teto do MEI, ...) com periodo de vigencia,
// para que um calculo de um mes antigo use o valor que valia naquele mes.
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "chave", "vigenciaInicio" }))
public class ParametroFiscal extends DefaultEntity {

    // identificador em maiusculas, ex.: SALARIO_MINIMO, TETO_MEI
    @Column(length = 60, nullable = false)
    private String chave;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate vigenciaInicio;

    // nula = sem data de fim (continua valendo)
    private LocalDate vigenciaFim;

    // vale na data informada: ja comecou e (nao tem fim ou ainda nao terminou)
    public boolean isVigenteEm(LocalDate data) {
        return !vigenciaInicio.isAfter(data) && (vigenciaFim == null || !vigenciaFim.isBefore(data));
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }

    public void setVigenciaInicio(LocalDate vigenciaInicio) {
        this.vigenciaInicio = vigenciaInicio;
    }

    public LocalDate getVigenciaFim() {
        return vigenciaFim;
    }

    public void setVigenciaFim(LocalDate vigenciaFim) {
        this.vigenciaFim = vigenciaFim;
    }
}
