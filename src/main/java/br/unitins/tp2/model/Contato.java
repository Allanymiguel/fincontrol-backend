package br.unitins.tp2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Pessoa (fisica ou juridica) com quem o usuario movimenta dinheiro: cliente ou fornecedor.
@Entity
public class Contato extends DefaultEntity {

    // dono do contato: cada usuario so acessa os proprios (RN12)
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(nullable = false)
    private TipoContato tipo;

    // CPF (11 digitos) ou CNPJ (14 digitos), salvo somente com numeros; so no cadastro completo.
    // Unico por usuario (regra no service), nao no banco inteiro: dois usuarios podem ter o
    // mesmo fornecedor.
    @Column(length = 14)
    private String documento;

    @Column(length = 120)
    private String email;

    @Column(length = 11)
    private String telefone;

    // so no cadastro completo
    @Embedded
    private Endereco endereco;

    // cadastro completo = documento e endereco preenchidos; o simples tem nome, tipo e telefone
    public boolean isCadastroCompleto() {
        return documento != null && endereco != null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoContato getTipo() {
        return tipo;
    }

    public void setTipo(TipoContato tipo) {
        this.tipo = tipo;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
