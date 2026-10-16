package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

// Modelo de categoria mantido pelo admin; cada conta nova recebe uma copia das nao arquivadas (RN13).
// Nao e excluida, so arquivada, para nao quebrar as categorias que ja foram copiadas.
@Entity
public class CategoriaPadrao extends DefaultEntity {

    @Column(length = 60, nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoTransacao tipo;

    // hex #RRGGBB
    @Column(length = 7)
    private String cor;

    @Column(nullable = false)
    private Boolean arquivada;

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

    public Boolean getArquivada() {
        return arquivada;
    }

    public void setArquivada(Boolean arquivada) {
        this.arquivada = arquivada;
    }
}
