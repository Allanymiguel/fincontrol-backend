package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Endereco nao tem tabela propria: as colunas ficam na tabela de quem o usa (Usuario e Contato).
// Cidade e UF nao sao texto livre: ficam estruturados via Municipio -> Estado.
// Nos dois o endereco e opcional (cadastro simples), entao as colunas sao anulaveis; no cadastro
// completo a obrigatoriedade vem dos DTOs.
@Embeddable
public class Endereco {

    @Column(length = 8)
    private String cep;

    @Column(length = 120)
    private String logradouro;

    @Column(length = 10)
    private String numero;

    @Column(length = 60)
    private String complemento;

    @Column(length = 60)
    private String bairro;

    @ManyToOne
    @JoinColumn(name = "id_municipio")
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
