package br.unitins.tp2.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

// Quem fez o que e quando na area administrativa. So de insercao: @Immutable (o Hibernate nunca
// gera UPDATE) e nenhum endpoint altera ou apaga um evento.
// O admin e copiado como texto (sem FK): o registro continua legivel se a conta mudar depois.
// Privacidade: nunca guarda financas pessoais; documento e telefone aparecem so como "alterado".
@Entity
@Immutable
@Table(indexes = @Index(columnList = "dataHora"))
public class EventoAuditoria extends DefaultEntity {

    @Column(nullable = false)
    private LocalDateTime dataHora;

    // claim "sub" do token; nulo so no modo dev sem login
    @Column(length = 36)
    private String adminKeycloakId;

    @Column(length = 120, nullable = false)
    private String adminNome;

    @Column(length = 120)
    private String adminEmail;

    @Column(nullable = false)
    private AcaoAuditoria acao;

    @Column(nullable = false)
    private TipoAlvoAuditoria tipoAlvo;

    private Long idAlvo;

    // nome, e-mail, codigo ou chave do alvo, para a lista ser legivel sem consultar outra tabela
    @Column(length = 160)
    private String descricaoAlvo;

    // JSON: [{"campo", "antes", "depois", "oculto"}] so com os campos que mudaram
    @Column(columnDefinition = "text")
    private String alteracoes;

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getAdminKeycloakId() {
        return adminKeycloakId;
    }

    public void setAdminKeycloakId(String adminKeycloakId) {
        this.adminKeycloakId = adminKeycloakId;
    }

    public String getAdminNome() {
        return adminNome;
    }

    public void setAdminNome(String adminNome) {
        this.adminNome = adminNome;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public void setAcao(AcaoAuditoria acao) {
        this.acao = acao;
    }

    public TipoAlvoAuditoria getTipoAlvo() {
        return tipoAlvo;
    }

    public void setTipoAlvo(TipoAlvoAuditoria tipoAlvo) {
        this.tipoAlvo = tipoAlvo;
    }

    public Long getIdAlvo() {
        return idAlvo;
    }

    public void setIdAlvo(Long idAlvo) {
        this.idAlvo = idAlvo;
    }

    public String getDescricaoAlvo() {
        return descricaoAlvo;
    }

    public void setDescricaoAlvo(String descricaoAlvo) {
        this.descricaoAlvo = descricaoAlvo;
    }

    public String getAlteracoes() {
        return alteracoes;
    }

    public void setAlteracoes(String alteracoes) {
        this.alteracoes = alteracoes;
    }
}
