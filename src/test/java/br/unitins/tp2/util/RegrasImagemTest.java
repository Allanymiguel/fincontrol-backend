package br.unitins.tp2.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ValidationException;

// Regras das imagens dos planos: tipo pelo conteudo
class RegrasImagemTest {

    static final byte[] JPEG = bytes(0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 'J', 'F', 'I', 'F');
    static final byte[] PNG = bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D);
    static final byte[] WEBP = bytes('R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' ');

    @Test
    void detectaJpegPngEWebpPeloConteudo() {
        assertEquals(Optional.of(TipoImagem.JPEG), RegrasImagem.detectar(JPEG));
        assertEquals(Optional.of(TipoImagem.PNG), RegrasImagem.detectar(PNG));
        assertEquals(Optional.of(TipoImagem.WEBP), RegrasImagem.detectar(WEBP));
    }

    @Test
    void recusaGifPdfSvgERiffQueNaoEWebp() {
        assertNaoDetecta(texto("GIF89a\u0001\u0000\u0001\u0000"));
        assertNaoDetecta(texto("%PDF-1.7\n%"));
        assertNaoDetecta(texto("<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>"));
        // audio WAV tambem comeca com RIFF, mas tem WAVE no lugar de WEBP
        assertNaoDetecta(bytes('R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'A', 'V', 'E', 'f', 'm', 't', ' '));
    }

    @Test
    void recusaArquivoCurtoOuVazio() {
        assertNaoDetecta(bytes(0xFF, 0xD8));
        assertNaoDetecta(bytes('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E'));
        assertNaoDetecta(new byte[0]);
        assertNaoDetecta(null);
    }

    @Test
    void textoRenomeadoParaPngNaoPassa() {
        // o nome "foto.png" nao importa: so o conteudo decide
        assertNaoDetecta(texto("isto e um arquivo de texto renomeado para foto.png"));
    }

    @Test
    void contentTypeEExtensaoVemDoTipoDetectado() {
        assertEquals("image/jpeg", TipoImagem.JPEG.getContentType());
        assertEquals("jpg", TipoImagem.JPEG.getExtensao());
        assertEquals("image/png", TipoImagem.PNG.getContentType());
        assertEquals("webp", TipoImagem.WEBP.getExtensao());
    }

    @Test
    void limiteEDoisMiB() {
        assertEquals(2_097_152L, RegrasImagem.LIMITE_BYTES);
    }

    @Test
    void arquivoVazioERecusado() {
        assertErroNoCampoArquivo("O arquivo está vazio.", () -> RegrasImagem.validarTamanho(0));
        assertErroNoCampoArquivo("O arquivo está vazio.", () -> RegrasImagem.validar(new byte[0]));
    }

    @Test
    void exatamenteDoisMiBPassaEUmByteAMaisNao() {
        assertDoesNotThrow(() -> RegrasImagem.validarTamanho(RegrasImagem.LIMITE_BYTES));
        assertErroNoCampoArquivo("A imagem pode ter no máximo 2 MB.",
                () -> RegrasImagem.validarTamanho(RegrasImagem.LIMITE_BYTES + 1));
    }

    @Test
    void validarConfereOTamanhoDoConteudo() {
        assertEquals(TipoImagem.PNG, RegrasImagem.validar(comTamanho(PNG, (int) RegrasImagem.LIMITE_BYTES)));
        assertErroNoCampoArquivo("A imagem pode ter no máximo 2 MB.",
                () -> RegrasImagem.validar(comTamanho(PNG, (int) RegrasImagem.LIMITE_BYTES + 1)));
    }

    @Test
    void validarDevolveOTipoDetectadoOuRecusaOConteudo() {
        assertEquals(TipoImagem.JPEG, RegrasImagem.validar(JPEG));
        assertEquals(TipoImagem.WEBP, RegrasImagem.validar(WEBP));
        assertErroNoCampoArquivo("Envie uma imagem JPEG, PNG ou WebP.",
                () -> RegrasImagem.validar(texto("GIF89a\u0001\u0000\u0001\u0000")));
    }

    @Test
    void chaveTemOPlanoOUuidEAExtensaoDoTipoDetectado() {
        UUID uuid = UUID.fromString("3f2a1c9e-8b7d-4e6f-9a01-23456789abcd");
        assertEquals("planos/7/3f2a1c9e-8b7d-4e6f-9a01-23456789abcd.jpg", RegrasImagem.chave(7, TipoImagem.JPEG, uuid));
        assertEquals("planos/7/3f2a1c9e-8b7d-4e6f-9a01-23456789abcd.webp", RegrasImagem.chave(7, TipoImagem.WEBP, uuid));
    }

    @Test
    void cadaEnvioGeraUmaChaveNova() {
        assertNotEquals(RegrasImagem.chave(1, TipoImagem.PNG), RegrasImagem.chave(1, TipoImagem.PNG));
        assertTrue(RegrasImagem.chave(1, TipoImagem.PNG).matches("planos/1/[0-9a-f-]{36}\\.png"));
    }

    @Test
    void versaoEOUuidDaChave() {
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid.toString(), RegrasImagem.versao(RegrasImagem.chave(3, TipoImagem.PNG, uuid)));
        assertNull(RegrasImagem.versao(null));
        assertNull(RegrasImagem.versao(" "));
    }

    @Test
    void nomeParaDownloadTiraOCaminho() {
        assertEquals("foto.png", RegrasImagem.nomeParaDownload("C:\\fakepath\\foto.png", TipoImagem.PNG));
        assertEquals("passwd.png", RegrasImagem.nomeParaDownload("../../etc/passwd", TipoImagem.PNG));
    }

    @Test
    void nomeParaDownloadTiraQuebrasDeLinhaEAspas() {
        assertEquals("ab c.jpg", RegrasImagem.nomeParaDownload("a\r\nb \"c\".jpg", TipoImagem.JPEG));
        assertEquals("xy.jpg", RegrasImagem.nomeParaDownload("x;y.jpg", TipoImagem.JPEG));
    }

    @Test
    void nomeParaDownloadUsaAExtensaoDoTipoDetectado() {
        assertEquals("foto.jpg", RegrasImagem.nomeParaDownload("foto.png", TipoImagem.JPEG));
        assertEquals("logo.webp", RegrasImagem.nomeParaDownload("logo", TipoImagem.WEBP));
        assertEquals("plano.anual.png", RegrasImagem.nomeParaDownload("plano.anual.gif", TipoImagem.PNG));
    }

    @Test
    void nomeParaDownloadVazioOuSoComPontosUsaONomePadrao() {
        assertEquals("imagem-plano.png", RegrasImagem.nomeParaDownload(null, TipoImagem.PNG));
        assertEquals("imagem-plano.png", RegrasImagem.nomeParaDownload("   ", TipoImagem.PNG));
        assertEquals("imagem-plano.png", RegrasImagem.nomeParaDownload(".png", TipoImagem.PNG));
        assertEquals("imagem-plano.jpg", RegrasImagem.nomeParaDownload("C:\\pasta\\", TipoImagem.JPEG));
    }

    @Test
    void nomeParaDownloadLimitaABaseACemCaracteres() {
        String nome = RegrasImagem.nomeParaDownload("a".repeat(300) + ".png", TipoImagem.PNG);
        assertEquals("a".repeat(100) + ".png", nome);
    }

    @Test
    void contentDispositionInlineOuAttachmentComAcentosNoFilenameEstrela() {
        assertEquals("inline; filename=\"promocao.png\"; filename*=UTF-8''promo%C3%A7%C3%A3o.png",
                RegrasImagem.contentDisposition("promoção.png", false));
        assertEquals("attachment; filename=\"plano essencial.jpg\"; filename*=UTF-8''plano%20essencial.jpg",
                RegrasImagem.contentDisposition("plano essencial.jpg", true));
    }

    @Test
    void tamanhoLegivel() {
        assertEquals("850 bytes", RegrasImagem.tamanhoLegivel(850));
        assertEquals("512 KB", RegrasImagem.tamanhoLegivel(512 * 1024));
        assertEquals("1,5 MB", RegrasImagem.tamanhoLegivel(1536 * 1024));
        assertEquals("2,0 MB", RegrasImagem.tamanhoLegivel(RegrasImagem.LIMITE_BYTES));
    }

    @Test
    void urlComAVersaoAtualFicaEmCachePorUmAno() {
        assertEquals("public, max-age=31536000, immutable", RegrasImagem.cacheControl("abc", "abc"));
    }

    @Test
    void semVersaoOuComVersaoAntigaRevalida() {
        assertEquals("no-cache", RegrasImagem.cacheControl(null, "abc"));
        assertEquals("no-cache", RegrasImagem.cacheControl("antiga", "abc"));
    }

    @Test
    void etagForteEntreAspas() {
        assertEquals("\"3f2a1c9e-8b7d-4e6f-9a01-23456789abcd\"",
                RegrasImagem.etag("3f2a1c9e-8b7d-4e6f-9a01-23456789abcd"));
    }

    private static void assertErroNoCampoArquivo(String mensagem, Runnable acao) {
        ValidationException erro = assertThrows(ValidationException.class, acao::run);
        assertEquals(1, erro.getFieldErrors().size());
        Problem.FieldError campo = erro.getFieldErrors().get(0);
        assertEquals("arquivo", campo.field);
        assertEquals(mensagem, campo.message);
    }

    // conteudo com a assinatura do tipo e completado com zeros ate o tamanho pedido
    private static byte[] comTamanho(byte[] assinatura, int tamanho) {
        return Arrays.copyOf(assinatura, tamanho);
    }

    private static void assertNaoDetecta(byte[] conteudo) {
        assertTrue(RegrasImagem.detectar(conteudo).isEmpty());
    }

    static byte[] bytes(int... valores) {
        byte[] resultado = new byte[valores.length];
        for (int i = 0; i < valores.length; i++)
            resultado[i] = (byte) valores[i];
        return resultado;
    }

    static byte[] texto(String conteudo) {
        return conteudo.getBytes(StandardCharsets.ISO_8859_1);
    }
}
