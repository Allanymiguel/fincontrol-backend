package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Categoria extends DefaultEntity {

    @Column(length = 60, nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoTransacao tipo;

    @Column(length = 7, nullable = false)
    private String cor;

    @Column(nullable = false)
    private Boolean ativa;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

}
