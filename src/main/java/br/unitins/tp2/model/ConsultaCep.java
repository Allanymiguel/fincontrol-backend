package br.unitins.tp2.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

// Log das buscas de CEP do cadastro de clientes (uma linha por fonte consultada). So de insercao.
// Guarda so o CEP: nada liga a consulta a uma pessoa.
@Entity
@Immutable
@Table(indexes = @Index(columnList = "cep, dataHora"))
public class ConsultaCep extends DefaultEntity {

    @Column(length = 8, nullable = false)
    private String cep;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    private FonteConsultaCep fonte;

    @Column(nullable = false)
    private ResultadoConsultaCep resultado;

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public FonteConsultaCep getFonte() {
        return fonte;
    }

    public void setFonte(FonteConsultaCep fonte) {
        this.fonte = fonte;
    }

    public ResultadoConsultaCep getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoConsultaCep resultado) {
        this.resultado = resultado;
    }
}
