package br.unitins.tp2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

// Base propria de enderecos dos CLIENTES (usuarios do sistema). Alimentada so pelo cadastro de
// clientes, pelo ViaCEP e por importacao CSV. Enderecos de contatos nunca entram aqui (sao
// financas pessoais do usuario). Numero e complemento nunca sao guardados: sao de cada pessoa.
// dataCadastro e dataAlteracao (DefaultEntity) sao a criacao e a ultima atualizacao.
@Entity
public class CepLocal extends DefaultEntity {

    @Column(length = 8, nullable = false, unique = true)
    private String cep;

    // nulos num CEP geral de cidade (o ViaCEP devolve so a cidade): o cliente digita
    @Column(length = 120)
    private String logradouro;

    @Column(length = 60)
    private String bairro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

    @Column(nullable = false)
    private OrigemCep origem;

    @Column(nullable = false)
    private SituacaoCep situacao;

    // ultima revalidacao com o ViaCEP e o que ele devolveu (para mostrar a diferenca)
    private LocalDateTime dataRevalidacao;

    @Column(length = 120)
    private String logradouroExterno;

    @Column(length = 60)
    private String bairroExterno;

    @Column(length = 80)
    private String municipioExterno;

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

    public OrigemCep getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemCep origem) {
        this.origem = origem;
    }

    public SituacaoCep getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoCep situacao) {
        this.situacao = situacao;
    }

    public LocalDateTime getDataRevalidacao() {
        return dataRevalidacao;
    }

    public void setDataRevalidacao(LocalDateTime dataRevalidacao) {
        this.dataRevalidacao = dataRevalidacao;
    }

    public String getLogradouroExterno() {
        return logradouroExterno;
    }

    public void setLogradouroExterno(String logradouroExterno) {
        this.logradouroExterno = logradouroExterno;
    }

    public String getBairroExterno() {
        return bairroExterno;
    }

    public void setBairroExterno(String bairroExterno) {
        this.bairroExterno = bairroExterno;
    }

    public String getMunicipioExterno() {
        return municipioExterno;
    }

    public void setMunicipioExterno(String municipioExterno) {
        this.municipioExterno = municipioExterno;
    }
}
