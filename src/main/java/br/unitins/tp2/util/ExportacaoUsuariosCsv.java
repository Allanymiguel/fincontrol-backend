package br.unitins.tp2.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Lista de usuarios em CSV para abrir certo no Excel: UTF-8 com BOM e separador ";".
// Sem CPF/CNPJ, telefone e endereco (identifica pelo nome e e-mail).
public final class ExportacaoUsuariosCsv {

    public static final String BOM = "﻿";
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CABECALHO = String.join(";", "Nome", "E-mail", "Papel", "Situação", "Cadastro",
            "Plano", "Periodicidade", "Valor da cobrança", "Próxima cobrança", "Troca agendada", "Cadastrado em");

    private ExportacaoUsuariosCsv() {
    }

    public record Linha(String nome, String email, String papel, boolean ativo, boolean cadastroCompleto, String plano,
            String periodicidade, BigDecimal valor, LocalDate proximaCobranca, String trocaAgendada,
            LocalDateTime cadastradoEm) {
    }

    public static String gerar(List<Linha> linhas) {
        StringBuilder csv = new StringBuilder(BOM).append(CABECALHO).append("\r\n");
        for (Linha l : linhas) {
            csv.append(String.join(";",
                    campo(l.nome()),
                    campo(l.email()),
                    campo(l.papel()),
                    l.ativo() ? "Ativo" : "Desativado",
                    l.cadastroCompleto() ? "Completo" : "Incompleto",
                    campo(l.plano()),
                    campo(l.periodicidade()),
                    l.valor() == null ? "" : l.valor().setScale(2).toPlainString().replace('.', ','),
                    l.proximaCobranca() == null ? "" : l.proximaCobranca().format(DATA_BR),
                    campo(l.trocaAgendada()),
                    l.cadastradoEm() == null ? "" : l.cadastradoEm().format(DATA_BR)))
                    .append("\r\n");
        }
        return csv.toString();
    }

    // texto com ";", aspas ou quebra de linha vai entre aspas (aspas internas dobradas)
    static String campo(String valor) {
        if (valor == null)
            return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r"))
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        return valor;
    }
}
