package br.unitins.tp2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;

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

    // CPF (PF) ou CNPJ (MEI/ME), salvo somente com numeros
    @Column(length = 14)
    private String documento;

    @Column(length = 11)
    private String telefone;

    // nulo no cadastro simples (so nome, e-mail e senha)
    private PerfilFiscal perfilFiscal;

    // obrigatoria so quando perfilFiscal = MEI
    private AtividadeMei atividadeMei;

    @Column(nullable = false)
    private Papel papel;

    @Column(nullable = false)
    private Boolean ativo;

    // opcional (0..1 no UML): o cadastro simples nao tem endereco
    @Embedded
    private Endereco endereco;

    // quando o admin usou "encerrar sessoes" e os ids das sessoes encerradas por ele ou pelo logout
    // (claim "sid"), cujo token o ContaAtivaAugmentor recusa (separados por virgula, so os recentes)
    private LocalDateTime sessoesEncerradasEm;

    @Column(length = 2000)
    private String sessoesRevogadas;

    // cadastro completo = dados fiscais e endereco preenchidos; o simples tem so o acesso
    public boolean isCadastroCompleto() {
        return documento != null && perfilFiscal != null && endereco != null;
    }

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

    public LocalDateTime getSessoesEncerradasEm() {
        return sessoesEncerradasEm;
    }

    public void setSessoesEncerradasEm(LocalDateTime sessoesEncerradasEm) {
        this.sessoesEncerradasEm = sessoesEncerradasEm;
    }

    public String getSessoesRevogadas() {
        return sessoesRevogadas;
    }

    public void setSessoesRevogadas(String sessoesRevogadas) {
        this.sessoesRevogadas = sessoesRevogadas;
    }
}
