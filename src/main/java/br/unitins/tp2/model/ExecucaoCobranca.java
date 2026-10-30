package br.unitins.tp2.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

// Uma execucao da cobranca (automatica das 03:00 ou disparo manual do admin), gravada em transacao
// propria ao final, mesmo quando falha. So de insercao. Responde "a cobranca de ontem rodou direito?".
@Entity
@Immutable
@Table(indexes = @Index(columnList = "dataReferencia"))
public class ExecucaoCobranca extends DefaultEntity {

    // ate quando a execucao cobrou (inclusive)
    @Column(nullable = false)
    private LocalDate dataReferencia;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(nullable = false)
    private Long duracaoMs;

    @Column(nullable = false)
    private OrigemExecucaoCobranca origem;

    @Column(nullable = false)
    private StatusExecucaoCobranca status;

    // admin do disparo manual (copiado como texto); "Agendador" na automatica
    @Column(length = 36)
    private String adminKeycloakId;

    @Column(length = 160, nullable = false)
    private String disparadoPor;

    // totais do resumo (CobrancaResponseDTO)
    @Column(nullable = false)
    private Integer assinaturasProcessadas;

    @Column(nullable = false)
    private Integer pagamentosGerados;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private Integer trocasAplicadas;

    @Column(nullable = false)
    private Integer voltasAoGratuito;

    // tentativas recusadas (simulacao): nao sao erro da execucao. O default cobre as linhas do seed
    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer pagamentosRecusados = 0;

    // falha da execucao inteira (ex.: banco fora do ar); nulo se ela terminou
    @Column(length = 500)
    private String mensagemErro;

    // JSON: [{"idAssinatura", "usuario", "mensagem"}] das assinaturas que falharam (as outras seguiram)
    @Column(columnDefinition = "text")
    private String erros;

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public Long getDuracaoMs() {
        return duracaoMs;
    }

    public void setDuracaoMs(Long duracaoMs) {
        this.duracaoMs = duracaoMs;
    }

    public OrigemExecucaoCobranca getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemExecucaoCobranca origem) {
        this.origem = origem;
    }

    public StatusExecucaoCobranca getStatus() {
        return status;
    }

    public void setStatus(StatusExecucaoCobranca status) {
        this.status = status;
    }

    public String getAdminKeycloakId() {
        return adminKeycloakId;
    }

    public void setAdminKeycloakId(String adminKeycloakId) {
        this.adminKeycloakId = adminKeycloakId;
    }

    public String getDisparadoPor() {
        return disparadoPor;
    }

    public void setDisparadoPor(String disparadoPor) {
        this.disparadoPor = disparadoPor;
    }

    public Integer getAssinaturasProcessadas() {
        return assinaturasProcessadas;
    }

    public void setAssinaturasProcessadas(Integer assinaturasProcessadas) {
        this.assinaturasProcessadas = assinaturasProcessadas;
    }

    public Integer getPagamentosGerados() {
        return pagamentosGerados;
    }

    public void setPagamentosGerados(Integer pagamentosGerados) {
        this.pagamentosGerados = pagamentosGerados;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Integer getTrocasAplicadas() {
        return trocasAplicadas;
    }

    public void setTrocasAplicadas(Integer trocasAplicadas) {
        this.trocasAplicadas = trocasAplicadas;
    }

    public Integer getVoltasAoGratuito() {
        return voltasAoGratuito;
    }

    public void setVoltasAoGratuito(Integer voltasAoGratuito) {
        this.voltasAoGratuito = voltasAoGratuito;
    }

    public Integer getPagamentosRecusados() {
        return pagamentosRecusados;
    }

    public void setPagamentosRecusados(Integer pagamentosRecusados) {
        this.pagamentosRecusados = pagamentosRecusados;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }

    public String getErros() {
        return erros;
    }

    public void setErros(String erros) {
        this.erros = erros;
    }
}
