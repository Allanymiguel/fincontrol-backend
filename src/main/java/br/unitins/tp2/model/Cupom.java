package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;

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

    // quantas assinaturas podem aderir ao cupom; nulo = sem limite. Uso = adesao: a assinatura escolher
    // o cupom numa troca de plano (o agendamento desfeito antes de valer libera o uso)
    private Integer limiteUsos;

    // planos em que o cupom vale; vazio = todos
    @ManyToMany
    @JoinTable(name = "cupom_plano", joinColumns = @JoinColumn(name = "cupom_id"),
            inverseJoinColumns = @JoinColumn(name = "plano_id"))
    @OrderBy("precoMensal, nome")
    private Set<Plano> planos = new LinkedHashSet<>();

    public boolean valePara(Plano plano) {
        return planos.isEmpty()
                || planos.stream().anyMatch(p -> p == plano || (p.getId() != null && p.getId().equals(plano.getId())));
    }

    public Integer getLimiteUsos() {
        return limiteUsos;
    }

    public void setLimiteUsos(Integer limiteUsos) {
        this.limiteUsos = limiteUsos;
    }

    public Set<Plano> getPlanos() {
        return planos;
    }

    public void setPlanos(Set<Plano> planos) {
        this.planos = planos;
    }

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
