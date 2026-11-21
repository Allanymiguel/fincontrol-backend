package br.unitins.tp2.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Arquivo guardado no Object Storage S3: o banco guarda so os metadados e a chave do objeto no bucket.
// Nao tem tabela propria: as colunas ficam na tabela de quem o usa (Plano, com o prefixo "imagem").
// Todas anulaveis: sem arquivo, as colunas ficam nulas e o Hibernate devolve o campo como null.
// Imutavel: trocar o arquivo e substituir o objeto inteiro (setImagem no dono).
@Embeddable
public class ArquivoArmazenado {

    // chave no bucket (ex.: planos/1/<uuid>.png). Nunca sai da API: o front recebe so a versao.
    @Column(length = 200)
    private String chave;

    // nome original, ja limpo (RegrasImagem.nomeParaDownload)
    @Column(length = 120)
    private String nome;

    // Content-Type detectado pelo conteudo, nunca o informado pelo cliente
    @Column(length = 30)
    private String tipo;

    private Long tamanho;

    private LocalDateTime enviadoEm;

    protected ArquivoArmazenado() {
        // JPA
    }

    public ArquivoArmazenado(String chave, String nome, String tipo, long tamanho, LocalDateTime enviadoEm) {
        this.chave = chave;
        this.nome = nome;
        this.tipo = tipo;
        this.tamanho = tamanho;
        this.enviadoEm = enviadoEm;
    }

    public String getChave() {
        return chave;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }
}
