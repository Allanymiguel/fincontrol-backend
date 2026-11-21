package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

// ArmazenamentoService com um S3Client simulado (sem SeaweedFS)
class ArmazenamentoServiceTest {

    private S3Client s3;
    private ArmazenamentoService armazenamento;

    @BeforeEach
    void preparar() {
        s3 = mock(S3Client.class);
        armazenamento = new ArmazenamentoService();
        armazenamento.s3 = s3;
        armazenamento.bucket = "fincontrol-test";
    }

    @Test
    void enviarUsaBucketChaveContentTypeETamanho() {
        armazenamento.enviar("planos/1/abc.png", new byte[] { 1, 2, 3 }, "image/png");

        ArgumentCaptor<PutObjectRequest> pedido = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3).putObject(pedido.capture(), any(RequestBody.class));
        assertEquals("fincontrol-test", pedido.getValue().bucket());
        assertEquals("planos/1/abc.png", pedido.getValue().key());
        assertEquals("image/png", pedido.getValue().contentType());
        assertEquals(3L, pedido.getValue().contentLength());
    }

    @Test
    void falhaDoS3AoEnviarViraServicoExterno() {
        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("Connection refused"));

        ServicoExternoException erro = assertThrows(ServicoExternoException.class,
                () -> armazenamento.enviar("planos/1/abc.png", new byte[] { 1 }, "image/png"));
        assertEquals("O armazenamento de imagens está indisponível no momento. Tente novamente.", erro.getMessage());
    }

    @Test
    void baixarDevolveOsBytesDoObjeto() {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), new byte[] { 9, 8 }));

        assertArrayEquals(new byte[] { 9, 8 }, armazenamento.baixar("planos/1/abc.png"));
    }

    @Test
    void objetoInexistenteVira404() {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertThrows(ResourceNotFoundException.class, () -> armazenamento.baixar("planos/1/sumiu.png"));
    }

    @Test
    void respostaHttp404DoS3TambemVira404() {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).build());
        assertThrows(ResourceNotFoundException.class, () -> armazenamento.baixar("planos/1/sumiu.png"));
    }

    @Test
    void falhaDoS3AoBaixarViraServicoExterno() {
        when(s3.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(500).build());
        assertThrows(ServicoExternoException.class, () -> armazenamento.baixar("planos/1/abc.png"));
    }

    @Test
    void excluirApagaOObjetoDoBucket() {
        armazenamento.excluir("planos/1/abc.png");

        ArgumentCaptor<DeleteObjectRequest> pedido = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3).deleteObject(pedido.capture());
        assertEquals("fincontrol-test", pedido.getValue().bucket());
        assertEquals("planos/1/abc.png", pedido.getValue().key());
    }

    @Test
    void excluirSemFalharNaoLancaExcecao() {
        when(s3.deleteObject(any(DeleteObjectRequest.class))).thenThrow(SdkClientException.create("Connection refused"));

        assertDoesNotThrow(() -> armazenamento.excluirSemFalhar("planos/1/abc.png"));
    }

    @Test
    void excluirSemFalharIgnoraChaveNula() {
        armazenamento.excluirSemFalhar(null);
        verifyNoInteractions(s3);
    }
}
