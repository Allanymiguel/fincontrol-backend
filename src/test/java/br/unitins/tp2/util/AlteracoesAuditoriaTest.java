package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.dto.AlteracaoAuditoriaDTO;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.Usuario;

// "Antes e depois" da auditoria: so o que mudou, sem documento e telefone
class AlteracoesAuditoriaTest {

    @Test
    void edicaoGuardaSoOsCamposQueMudaram() {
        Map<String, String> antes = retrato("BEMVINDO10", "10,00", "Sim");
        Map<String, String> depois = retrato("BEMVINDO10", "15,00", "Não");

        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(antes, depois);

        assertEquals(List.of(
                new AlteracaoAuditoriaDTO("Desconto (%)", "10,00", "15,00", false),
                new AlteracaoAuditoriaDTO("Ativo", "Sim", "Não", false)), alteracoes);
    }

    @Test
    void edicaoSemMudancaNaoTemAlteracoes() {
        assertTrue(AlteracoesAuditoria.comparar(retrato("A", "1,00", "Sim"), retrato("A", "1,00", "Sim")).isEmpty());
    }

    @Test
    void criacaoTemTodosOsCamposSemValorAntes() {
        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(Map.of(), retrato("NOVO", "5,00", "Sim"));

        assertEquals(3, alteracoes.size());
        assertTrue(alteracoes.stream().allMatch(a -> a.antes() == null && a.depois() != null));
    }

    @Test
    void exclusaoTemTodosOsCamposSemValorDepois() {
        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(retrato("VELHO", "5,00", "Não"), Map.of());

        assertEquals(3, alteracoes.size());
        assertTrue(alteracoes.stream().allMatch(a -> a.antes() != null && a.depois() == null));
    }

    @Test
    void documentoETelefoneAparecemSoComoAlteradosSemValores() {
        Usuario antes = usuario("11144477735", "63999991111");
        Usuario depois = usuario("39053344705", "63988887777");

        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(RetratoAuditoria.de(antes),
                RetratoAuditoria.de(depois));

        assertEquals(List.of(
                new AlteracaoAuditoriaDTO(AlteracoesAuditoria.DOCUMENTO, null, null, true),
                new AlteracaoAuditoriaDTO(AlteracoesAuditoria.TELEFONE, null, null, true)), alteracoes);
        String json = AlteracoesAuditoria.paraJson(alteracoes);
        assertFalse(json.contains("11144477735") || json.contains("39053344705") || json.contains("6399999"));
    }

    @Test
    void documentoQueNaoMudouNaoApareceNaAuditoria() {
        Usuario antes = usuario("11144477735", null);
        Usuario depois = usuario("11144477735", null);
        depois.setNome("Outro Nome");

        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(RetratoAuditoria.de(antes),
                RetratoAuditoria.de(depois));

        assertEquals(List.of(new AlteracaoAuditoriaDTO("Nome", "Fulano", "Outro Nome", false)), alteracoes);
    }

    @Test
    void jsonIdaEVolta() {
        List<AlteracaoAuditoriaDTO> alteracoes = List.of(
                new AlteracaoAuditoriaDTO("Ativo", "Sim", "Não", false),
                new AlteracaoAuditoriaDTO(AlteracoesAuditoria.TELEFONE, null, null, true));

        assertEquals(alteracoes, AlteracoesAuditoria.deJson(AlteracoesAuditoria.paraJson(alteracoes)));
        assertEquals(List.of(), AlteracoesAuditoria.deJson(null));
    }

    @Test
    void retratoDoCupomFormataValoresParaLeitura() {
        Cupom cupom = new Cupom();
        cupom.setCodigo("BLACKFRIDAY");
        cupom.setPercentualDesconto(new BigDecimal("30"));
        cupom.setValidade(java.time.LocalDate.of(2026, 11, 30));
        cupom.setAtivo(true);

        assertEquals(Map.of("Código", "BLACKFRIDAY", "Desconto (%)", "30,00", "Validade", "30/11/2026", "Ativo", "Sim",
                "Limite de usos", "Sem limite", "Planos", "Todos"), RetratoAuditoria.de(cupom));

        Plano essencial = new Plano();
        essencial.setNome("Essencial");
        cupom.setLimiteUsos(100);
        cupom.getPlanos().add(essencial);
        assertEquals("100", RetratoAuditoria.de(cupom).get("Limite de usos"));
        assertEquals("Essencial", RetratoAuditoria.de(cupom).get("Planos"));
    }

    private static Map<String, String> retrato(String codigo, String desconto, String ativo) {
        Map<String, String> retrato = new LinkedHashMap<>();
        retrato.put("Código", codigo);
        retrato.put("Desconto (%)", desconto);
        retrato.put("Ativo", ativo);
        return retrato;
    }

    private static Usuario usuario(String documento, String telefone) {
        Usuario usuario = new Usuario();
        usuario.setNome("Fulano");
        usuario.setEmail("fulano@email.com");
        usuario.setPapel(Papel.USER);
        usuario.setAtivo(true);
        usuario.setDocumento(documento);
        usuario.setTelefone(telefone);
        return usuario;
    }

    // ===== imagem do plano =====

    @Test
    void criacaoDePlanoSemImagemNaoTemLinhaDeImagem() {
        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(Map.of(), RetratoAuditoria.de(planoCom(null)));

        assertTrue(alteracoes.stream().noneMatch(a -> a.campo().equals("Imagem")));
    }

    @Test
    void envioDeImagemMostraNomeTamanhoEData() {
        Plano antes = planoCom(null);
        Plano depois = planoCom(arquivo("logo.png", 512 * 1024, LocalDateTime.of(2026, 11, 21, 14, 3, 27)));

        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(RetratoAuditoria.de(antes),
                RetratoAuditoria.de(depois));

        assertEquals(1, alteracoes.size());
        assertEquals("Imagem", alteracoes.get(0).campo());
        assertEquals(null, alteracoes.get(0).antes());
        assertEquals("logo.png (512 KB, enviada em 21/11/2026 14:03:27)", alteracoes.get(0).depois());
    }

    @Test
    void trocarPorArquivoDeMesmoNomeETamanhoAindaApareceNaAuditoria() {
        Plano antes = planoCom(arquivo("logo.png", 1000, LocalDateTime.of(2026, 11, 21, 14, 3, 27)));
        Plano depois = planoCom(arquivo("logo.png", 1000, LocalDateTime.of(2026, 11, 21, 14, 3, 28)));

        assertEquals(1, AlteracoesAuditoria.comparar(RetratoAuditoria.de(antes), RetratoAuditoria.de(depois)).size());
    }

    @Test
    void remocaoDaImagemFicaComDepoisNulo() {
        Plano antes = planoCom(arquivo("logo.png", 1000, LocalDateTime.of(2026, 11, 21, 14, 3, 27)));

        List<AlteracaoAuditoriaDTO> alteracoes = AlteracoesAuditoria.comparar(RetratoAuditoria.de(antes),
                RetratoAuditoria.de(planoCom(null)));

        assertEquals(1, alteracoes.size());
        assertEquals("logo.png (1000 bytes, enviada em 21/11/2026 14:03:27)", alteracoes.get(0).antes());
        assertEquals(null, alteracoes.get(0).depois());
    }

    private static ArquivoArmazenado arquivo(String nome, long tamanho, LocalDateTime enviadoEm) {
        return new ArquivoArmazenado("planos/1/abc.png", nome, "image/png", tamanho, enviadoEm);
    }

    private static Plano planoCom(ArquivoArmazenado imagem) {
        Plano plano = new Plano();
        plano.setNome("Essencial");
        plano.setPrecoMensal(new BigDecimal("19.90"));
        plano.setPrecoAnual(new BigDecimal("199.00"));
        plano.setLimiteTransacoes(1000);
        plano.setLimiteMetas(10);
        plano.setLiberaRecorrencias(true);
        plano.setLiberaFiscal(false);
        plano.setLiberaExportacao(true);
        plano.setAtivo(true);
        plano.setImagem(imagem);
        return plano;
    }
}
