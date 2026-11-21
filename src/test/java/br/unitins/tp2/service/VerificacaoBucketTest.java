package br.unitins.tp2.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

// Verificacao do bucket na subida, com um S3Client simulado
class VerificacaoBucketTest {

    private S3Client s3;
    private VerificacaoBucket verificacao;

    @BeforeEach
    void preparar() {
        s3 = mock(S3Client.class);
        verificacao = new VerificacaoBucket();
        verificacao.s3 = s3;
        verificacao.bucket = "fincontrol";
        verificacao.verificarNaSubida = true;
    }

    @Test
    void bucketAusenteECriado() {
        when(s3.headBucket(any(HeadBucketRequest.class))).thenThrow(NoSuchBucketException.builder().statusCode(404).build());

        verificacao.verificar();

        ArgumentCaptor<CreateBucketRequest> pedido = ArgumentCaptor.forClass(CreateBucketRequest.class);
        verify(s3).createBucket(pedido.capture());
        assertEquals("fincontrol", pedido.getValue().bucket());
    }

    @Test
    void bucketPresenteNaoFazNada() {
        verificacao.verificar();

        verify(s3).headBucket(any(HeadBucketRequest.class));
        verify(s3, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void s3ForaDoArSoAvisaEOAppSobe() {
        when(s3.headBucket(any(HeadBucketRequest.class))).thenThrow(SdkClientException.create("Connection refused"));

        assertDoesNotThrow(verificacao::verificar);
        verify(s3, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void falhaAoCriarOBucketSoAvisa() {
        when(s3.headBucket(any(HeadBucketRequest.class))).thenThrow(NoSuchBucketException.builder().statusCode(404).build());
        when(s3.createBucket(any(CreateBucketRequest.class))).thenThrow(SdkClientException.create("Connection refused"));

        assertDoesNotThrow(verificacao::verificar);
    }

    @Test
    void desligadoNaoChamaOS3() {
        verificacao.verificarNaSubida = false;

        verificacao.aoSubir(null);

        verifyNoInteractions(s3);
    }
}
