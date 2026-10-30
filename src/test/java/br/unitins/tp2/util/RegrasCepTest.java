package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.Estado;
import br.unitins.tp2.model.FaixaCepUf;
import br.unitins.tp2.model.Municipio;

// Regras da base propria de enderecos de clientes
class RegrasCepTest {

    private final Estado to = estado("TO");
    private final Estado df = estado("DF");
    private final Estado go = estado("GO");
    private final List<FaixaCepUf> faixas = List.of(
            faixa(to, "77000000", "77999999"),
            faixa(df, "70000000", "72799999"),
            faixa(go, "72800000", "72999999"),
            faixa(df, "73000000", "73699999"),
            faixa(go, "73700000", "76799999"));

    @Test
    void normalizaCepComMascaraERecusaTamanhoErrado() {
        assertEquals("77001002", RegrasCep.normalizar("77001-002"));
        assertEquals("77001002", RegrasCep.normalizar(" 77.001-002 "));
        assertNull(RegrasCep.normalizar("7700100"));
        assertNull(RegrasCep.normalizar(null));
    }

    @Test
    void ufPelaFaixaDoCepInclusiveNasBordas() {
        assertEquals("TO", RegrasCep.ufDoCep(faixas, "77000000"));
        assertEquals("TO", RegrasCep.ufDoCep(faixas, "77999999"));
        assertEquals("TO", RegrasCep.ufDoCep(faixas, "77060000"));
    }

    @Test
    void ufComDuasFaixasIntercaladas() {
        assertEquals("DF", RegrasCep.ufDoCep(faixas, "70040000"));
        assertEquals("GO", RegrasCep.ufDoCep(faixas, "72850000"));
        assertEquals("DF", RegrasCep.ufDoCep(faixas, "73010000"));
        assertEquals("GO", RegrasCep.ufDoCep(faixas, "74000000"));
    }

    @Test
    void cepForaDeQualquerFaixaNaoTemUf() {
        assertNull(RegrasCep.ufDoCep(faixas, "00000000"));
        assertNull(RegrasCep.ufDoCep(faixas, "78000000"));
    }

    @Test
    void manualSoComApiDesligadaOuFalhaRecente() {
        assertTrue(RegrasCep.aceitaManual(false, false), "API desligada");
        assertTrue(RegrasCep.aceitaManual(true, true), "API ligada, falhou ou nao achou ha pouco");
        assertFalse(RegrasCep.aceitaManual(true, false), "API ligada e sem falha recente: precisa consultar");
    }

    @Test
    void janelaDeFalhaRecenteEDeTrintaMinutos() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 30, 14, 0);
        assertEquals(LocalDateTime.of(2026, 10, 30, 13, 30), RegrasCep.inicioDaJanela(agora));
    }

    @Test
    void municipioPrecisaSerDaUfDoCep() {
        Municipio palmas = municipio("Palmas", to);
        Municipio goiania = municipio("Goiânia", go);

        assertTrue(RegrasCep.municipioCompativel(palmas, "TO"));
        assertTrue(RegrasCep.municipioCompativel(palmas, "to"));
        assertFalse(RegrasCep.municipioCompativel(goiania, "TO"));
        assertFalse(RegrasCep.municipioCompativel(null, "TO"));
        assertFalse(RegrasCep.municipioCompativel(palmas, null));
    }

    @Test
    void revalidacaoIgnoraAcentoMaiusculaEEspacos() {
        assertTrue(RegrasCep.confere("Praça Girassóis", "praca  girassois"));
        assertTrue(RegrasCep.confere(null, ""));
        assertFalse(RegrasCep.confere("Rua Um", "Rua 1"));
        assertFalse(RegrasCep.confere("Taquaralto", "Centro (Taquaralto)"));
    }

    @Test
    void linhaCsvValida() {
        RegrasCep.LinhaCsv linha = RegrasCep.lerLinha("77001-002;\"Praça Girassóis\";Plano Diretor Norte;1721000");

        assertEquals(new RegrasCep.LinhaCsv("77001002", "Praça Girassóis", "Plano Diretor Norte", "1721000"), linha);
    }

    @Test
    void linhaCsvDeCepGeralAceitaLogradouroEBairroVazios() {
        assertEquals(new RegrasCep.LinhaCsv("77380000", null, null, "1717008"),
                RegrasCep.lerLinha("77380000;;;1717008"));
    }

    @Test
    void linhasCsvInvalidasTrazemOMotivo() {
        assertTrue(motivo("77001002;Rua;Bairro").contains("4 colunas"));
        assertTrue(motivo("7700;Rua;Bairro;1721000").contains("CEP inválido"));
        assertTrue(motivo("77001002;Rua;Bairro;172100").contains("Código IBGE"));
        assertTrue(motivo("77001002;" + "x".repeat(121) + ";Bairro;1721000").contains("Logradouro"));
    }

    @Test
    void cabecalhoCsvObrigatorioAceitandoBom() {
        assertTrue(RegrasCep.cabecalhoValido("﻿cep;logradouro;bairro;codigo_ibge"));
        assertTrue(RegrasCep.cabecalhoValido("CEP; Logradouro; Bairro; Codigo_IBGE"));
        assertFalse(RegrasCep.cabecalhoValido("cep,logradouro,bairro,codigo_ibge"));
        assertFalse(RegrasCep.cabecalhoValido(null));
    }

    private static String motivo(String linha) {
        return assertThrows(IllegalArgumentException.class, () -> RegrasCep.lerLinha(linha)).getMessage();
    }

    private static Estado estado(String sigla) {
        Estado estado = new Estado();
        estado.setSigla(sigla);
        estado.setNome(sigla);
        return estado;
    }

    private static FaixaCepUf faixa(Estado estado, String inicial, String fim) {
        FaixaCepUf faixa = new FaixaCepUf();
        faixa.setEstado(estado);
        faixa.setCepInicial(inicial);
        faixa.setCepFinal(fim);
        return faixa;
    }

    private static Municipio municipio(String nome, Estado estado) {
        Municipio municipio = new Municipio();
        municipio.setNome(nome);
        municipio.setEstado(estado);
        return municipio;
    }
}
