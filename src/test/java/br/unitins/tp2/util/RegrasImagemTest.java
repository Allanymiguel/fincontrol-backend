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
