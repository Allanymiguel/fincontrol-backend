package br.unitins.tp2.util;

import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

import br.unitins.tp2.model.FaixaCepUf;
import br.unitins.tp2.model.Municipio;

// Regras da base propria de enderecos de clientes, sem banco (testaveis isoladamente)
public final class RegrasCep {

    // "recente" para liberar o endereco manual: o ViaCEP falhou ou nao achou o CEP nos ultimos 30 minutos
    public static final Duration JANELA_FALHA_RECENTE = Duration.ofMinutes(30);

    // importacao CSV: cep;logradouro;bairro;codigo_ibge, UTF-8, ate 1 MB ou 5.000 linhas
    public static final String CABECALHO_CSV = "cep;logradouro;bairro;codigo_ibge";
    public static final long MAX_BYTES_CSV = 1024 * 1024;
    public static final int MAX_LINHAS_CSV = 5000;

    private RegrasCep() {
    }

    // so os digitos; nulo se nao sobrarem exatamente 8
    public static String normalizar(String cep) {
        if (cep == null)
            return null;
        String digitos = cep.replaceAll("\\D", "");
        return digitos.length() == 8 ? digitos : null;
    }

    // sigla da UF cuja faixa contem o CEP; nulo se nenhuma contem
    public static String ufDoCep(Collection<FaixaCepUf> faixas, String cep) {
        for (FaixaCepUf faixa : faixas) {
            if (faixa.contem(cep))
                return faixa.getEstado().getSigla();
        }
        return null;
    }

    public static LocalDateTime inicioDaJanela(LocalDateTime agora) {
        return agora.minus(JANELA_FALHA_RECENTE);
    }

    // Endereco digitado so vale para CEP fora da base e quando a consulta externa esta desligada
    // ou falhou/nao achou o CEP ha pouco. Com a API ligada e sem falha recente, o CEP deve ser consultado.
    public static boolean aceitaManual(boolean apiExternaHabilitada, boolean falhaOuNaoEncontradoRecente) {
        return !apiExternaHabilitada || falhaOuNaoEncontradoRecente;
    }

    // o municipio escolhido precisa ser da UF da faixa do CEP
    public static boolean municipioCompativel(Municipio municipio, String ufDoCep) {
        return municipio != null && municipio.getEstado() != null && ufDoCep != null
                && ufDoCep.equalsIgnoreCase(municipio.getEstado().getSigla());
    }

    public static String vazioParaNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    // revalidacao: mesmo texto ignorando espacos, maiusculas e acentos; vazio = nulo
    public static boolean confere(String local, String externo) {
        return chave(local).equals(chave(externo));
    }

    private static String chave(String valor) {
        String texto = vazioParaNulo(valor);
        if (texto == null)
            return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase().replaceAll("\\s+", " ");
    }

    // ===== importacao CSV =====

    public record LinhaCsv(String cep, String logradouro, String bairro, String codigoIbge) {
    }

    // valida o formato de uma linha (sem cabecalho); o motivo vai na mensagem da excecao
    public static LinhaCsv lerLinha(String linha) {
        String[] colunas = linha.split(";", -1);
        if (colunas.length != 4)
            throw new IllegalArgumentException("A linha deve ter 4 colunas separadas por \";\" (tem " + colunas.length + ").");
        String cep = normalizar(semAspas(colunas[0]));
        if (cep == null)
            throw new IllegalArgumentException("CEP inválido: deve ter 8 dígitos.");
        String logradouro = vazioParaNulo(semAspas(colunas[1]));
        String bairro = vazioParaNulo(semAspas(colunas[2]));
        String codigoIbge = semAspas(colunas[3]).trim();
        if (!codigoIbge.matches("\\d{7}"))
            throw new IllegalArgumentException("Código IBGE inválido: deve ter 7 dígitos.");
        if (logradouro != null && logradouro.length() > 120)
            throw new IllegalArgumentException("Logradouro com mais de 120 caracteres.");
        if (bairro != null && bairro.length() > 60)
            throw new IllegalArgumentException("Bairro com mais de 60 caracteres.");
        return new LinhaCsv(cep, logradouro, bairro, codigoIbge);
    }

    // cabecalho obrigatorio (ignora BOM, espacos e maiusculas)
    public static boolean cabecalhoValido(String primeiraLinha) {
        return primeiraLinha != null
                && primeiraLinha.replace("﻿", "").trim().replace(" ", "").equalsIgnoreCase(CABECALHO_CSV);
    }

    private static String semAspas(String valor) {
        String texto = valor.trim();
        if (texto.length() >= 2 && texto.startsWith("\"") && texto.endsWith("\""))
            return texto.substring(1, texto.length() - 1).replace("\"\"", "\"");
        return texto;
    }
}
