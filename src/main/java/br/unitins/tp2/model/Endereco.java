package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Endereco nao tem tabela propria: as colunas ficam na tabela de quem o usa (Contato).
// Cidade e UF nao sao texto livre: ficam estruturados via Municipio -> Estado.
@Embeddable
public class Endereco {

    @Column(length = 8, nullable = false)
    private String cep;

    @Column(length = 120, nullable = false)
    private String logradouro;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(length = 60, nullable = false)
    private String bairro;

    // obrigatoriedade so via @JoinColumn (e nao optional = false) para que o Usuario,
    // que tem endereco opcional, consiga sobrescrever a coluna como anulavel
    @ManyToOne
    @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }
}
