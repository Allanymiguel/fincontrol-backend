package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.testes.Usuarios;
import br.unitins.tp2.util.RegrasImagem;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;

// Envio da imagem do plano: S3 simulado, banco de verdade (Dev Services). Cada teste cria o proprio plano.
@QuarkusTest
@TestSecurity(user = Usuarios.ADMIN, roles = "ADMIN")
@OidcSecurity(claims = @Claim(key = "sub", value = Usuarios.ADMIN_SUB))
class PlanoServiceImagemTest {

    static final byte[] PNG = Arrays.copyOf(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A }, 64);
    static final byte[] JPEG = Arrays.copyOf(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }, 64);

    @Inject
    PlanoService service;

    @InjectMock
    ArmazenamentoService armazenamento;

    @InjectSpy
    AuditoriaService auditoria;

    @Test
    void enviaOObjetoNovoEGravaOsMetadados() throws IOException {
        long id = criarPlano();

        Plano plano = service.enviarImagem(id, "C:\\fakepath\\Logo Plano.png", PNG.length, arquivo(PNG));

        ArgumentCaptor<String> chave = ArgumentCaptor.forClass(String.class);
        verify(armazenamento).enviar(chave.capture(), eq(PNG), eq("image/png"));
        assertTrue(chave.getValue().matches("planos/" + id + "/[0-9a-f-]{36}\\.png"), chave.getValue());

        ArquivoArmazenado imagem = service.findById(id).getImagem();
        assertEquals(chave.getValue(), imagem.getChave());
        assertEquals("Logo Plano.png", imagem.getNome());
        assertEquals("image/png", imagem.getTipo());
        assertEquals(64L, imagem.getTamanho());
        assertEquals(chave.getValue(), plano.getImagem().getChave());
        verify(auditoria).registrar(eq(AcaoAuditoria.ENVIO_IMAGEM_PLANO), eq(TipoAlvoAuditoria.PLANO), eq(id),
                anyString(), any(), any());
    }

    @Test
    void semImagemAnteriorNadaEApagado() throws IOException {
        long id = criarPlano();

        service.enviarImagem(id, "logo.png", PNG.length, arquivo(PNG));

        verify(armazenamento, never()).excluirSemFalhar(anyString());
    }

    @Test
    void trocarApagaOObjetoAntigoDepoisDeGravarONovo() throws IOException {
        long id = criarPlano();
        String chaveAntiga = service.enviarImagem(id, "logo.png", PNG.length, arquivo(PNG)).getImagem().getChave();

        Plano trocado = service.enviarImagem(id, "foto.png", JPEG.length, arquivo(JPEG));

        String chaveNova = trocado.getImagem().getChave();
        assertNotEquals(chaveAntiga, chaveNova);
        // o tipo vem do conteudo: o JPEG enviado como .png vira .jpg
        assertTrue(chaveNova.endsWith(".jpg"));
        assertEquals("foto.jpg", trocado.getImagem().getNome());
        InOrder ordem = inOrder(armazenamento);
        ordem.verify(armazenamento).enviar(eq(chaveNova), any(), eq("image/jpeg"));
        ordem.verify(armazenamento).excluirSemFalhar(chaveAntiga);
        assertEquals(chaveNova, service.findById(id).getImagem().getChave());
    }

    @Test
    void falhaAoGravarApagaOObjetoNovoENaoMudaOPlano() throws IOException {
        long id = criarPlano();
        doThrow(new IllegalStateException("falha simulada")).when(auditoria).registrar(
                eq(AcaoAuditoria.ENVIO_IMAGEM_PLANO), any(), any(), any(), any(), any());

        assertThrows(IllegalStateException.class, () -> service.enviarImagem(id, "logo.png", PNG.length, arquivo(PNG)));

        ArgumentCaptor<String> enviada = ArgumentCaptor.forClass(String.class);
        verify(armazenamento).enviar(enviada.capture(), any(), any());
        verify(armazenamento).excluirSemFalhar(enviada.getValue());
        assertNull(service.findById(id).getImagem());
    }

    @Test
    void s3ForaDoArDa503ENaoMudaOPlano() throws IOException {
        long id = criarPlano();
        doThrow(new ServicoExternoException("indisponivel", null)).when(armazenamento).enviar(anyString(), any(), anyString());

        assertThrows(ServicoExternoException.class, () -> service.enviarImagem(id, "logo.png", PNG.length, arquivo(PNG)));

        assertNull(service.findById(id).getImagem());
        verify(armazenamento, never()).excluirSemFalhar(anyString());
    }

    @Test
    void planoInexistenteDa404SemEnviar() throws IOException {
        Path arquivo = arquivo(PNG);

        assertThrows(ResourceNotFoundException.class, () -> service.enviarImagem(999_999, "logo.png", PNG.length, arquivo));

        verify(armazenamento, never()).enviar(anyString(), any(), anyString());
    }

    @Test
    void arquivoInvalidoDa400SemEnviar() throws IOException {
        long id = criarPlano();
        byte[] gif = "GIF89a....".getBytes(StandardCharsets.ISO_8859_1);
        Path arquivoGif = arquivo(gif);

        ValidationException tipo = assertThrows(ValidationException.class,
                () -> service.enviarImagem(id, "animacao.gif", gif.length, arquivoGif));
        assertEquals("arquivo", tipo.getFieldErrors().get(0).field);

        // grande demais: recusado pelo tamanho informado, antes de ler o arquivo
        Path pequeno = arquivo(PNG);
        assertThrows(ValidationException.class,
                () -> service.enviarImagem(id, "enorme.png", RegrasImagem.LIMITE_BYTES + 1, pequeno));

        verify(armazenamento, never()).enviar(anyString(), any(), anyString());
    }

    private long criarPlano() {
        PlanoRequestDTO dto = new PlanoRequestDTO("Teste " + UUID.randomUUID().toString().substring(0, 8),
                new BigDecimal("9.90"), new BigDecimal("99.00"), 100, 1, false, false, false, true);
        return service.create(dto).getId();
    }

    private static Path arquivo(byte[] conteudo) throws IOException {
        Path arquivo = Files.createTempFile("upload-", ".tmp");
        arquivo.toFile().deleteOnExit();
        return Files.write(arquivo, conteudo);
    }
}
