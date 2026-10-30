package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

// CSV da lista de usuarios: abre certo no Excel e nao leva CPF, telefone nem endereco
class ExportacaoUsuariosCsvTest {

    private final ExportacaoUsuariosCsv.Linha carla = new ExportacaoUsuariosCsv.Linha("Carla Mendes",
            "carla@email.com", "Usuário", true, true, "Essencial", "Mensal", new BigDecimal("19.9"),
            LocalDate.of(2026, 11, 10), null, LocalDateTime.of(2026, 3, 2, 8, 40));

    @Test
    void comecaComBomECabecalhoSeparadoPorPontoEVirgula() {
        String csv = ExportacaoUsuariosCsv.gerar(List.of());

        assertTrue(csv.startsWith(ExportacaoUsuariosCsv.BOM + "Nome;E-mail;Papel;Situação;Cadastro;Plano"));
    }

    @Test
    void linhaComValoresNoFormatoBrasileiro() {
        String[] linhas = ExportacaoUsuariosCsv.gerar(List.of(carla)).split("\r\n");

        assertEquals("Carla Mendes;carla@email.com;Usuário;Ativo;Completo;Essencial;Mensal;19,90;10/11/2026;;02/03/2026",
                linhas[1]);
    }

    @Test
    void naoTemColunaDeDocumentoTelefoneOuEndereco() {
        String cabecalho = ExportacaoUsuariosCsv.gerar(List.of()).split("\r\n")[0].toLowerCase();

        assertFalse(cabecalho.contains("cpf") || cabecalho.contains("documento") || cabecalho.contains("telefone")
                || cabecalho.contains("cep") || cabecalho.contains("endere"));
    }

    @Test
    void textoComPontoEVirgulaOuAspasVaiEntreAspas() {
        assertEquals("\"Silva; Souza\"", ExportacaoUsuariosCsv.campo("Silva; Souza"));
        assertEquals("\"O \"\"Zé\"\"\"", ExportacaoUsuariosCsv.campo("O \"Zé\""));
        assertEquals("Simples", ExportacaoUsuariosCsv.campo("Simples"));
        assertEquals("", ExportacaoUsuariosCsv.campo(null));
    }
}
