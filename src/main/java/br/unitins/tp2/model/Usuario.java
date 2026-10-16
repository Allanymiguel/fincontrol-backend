package br.unitins.tp2.model;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;

// Dono dos dados do sistema. O login e a senha ficam no Keycloak; aqui ficam o perfil
// (dados fiscais, endereco, papel) e o vinculo com o usuario do Keycloak (keycloakId).
@Entity
public class Usuario extends DefaultEntity {

    // id do usuario no Keycloak = claim "sub" do token. Nao muda, mesmo se o email mudar.
    @Column(length = 36, unique = true)
    private String keycloakId;

    @Column(length = 120, nullable = false)
    private String nome;

    @Column(length = 120, nullable = false, unique = true)
    private String email;

    // fica nulo enquanto a autenticacao for do Keycloak (que guarda a senha dele)
    @Column(length = 60)
    private String senhaHash;

    // CPF (PF) ou CNPJ (MEI/ME), salvo somente com numeros
    @Column(length = 14)
    private String documento;

    @Column(length = 11)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PerfilFiscal perfilFiscal;

    // obrigatoria so quando perfilFiscal = MEI
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AtividadeMei atividadeMei;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Papel papel;

    @Column(nullable = false)
    private Boolean ativo;

    // No Contato o endereco e obrigatorio; no usuario e opcional (0..1 no UML),
    // por isso as colunas do Endereco sao sobrescritas aqui como anulaveis.
    @Embedded
    @AttributeOverride(name = "cep", column = @Column(name = "cep", length = 8))
    @AttributeOverride(name = "logradouro", column = @Column(name = "logradouro", length = 120))
    @AttributeOverride(name = "numero", column = @Column(name = "numero", length = 10))
    @AttributeOverride(name = "complemento", column = @Column(name = "complemento", length = 60))
    @AttributeOverride(name = "bairro", column = @Column(name = "bairro", length = 60))
    @AssociationOverride(name = "municipio", joinColumns = @JoinColumn(name = "id_municipio"))
    private Endereco endereco;

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public PerfilFiscal getPerfilFiscal() {
        return perfilFiscal;
    }

    public void setPerfilFiscal(PerfilFiscal perfilFiscal) {
        this.perfilFiscal = perfilFiscal;
    }

    public AtividadeMei getAtividadeMei() {
        return atividadeMei;
    }

    public void setAtividadeMei(AtividadeMei atividadeMei) {
        this.atividadeMei = atividadeMei;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
