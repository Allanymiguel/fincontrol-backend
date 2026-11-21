package br.unitins.tp2.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

// Object Storage S3 (SeaweedFS em dev): guarda, le e apaga objetos do bucket do FinControl.
// Nao conhece regra de negocio: quem decide chave, tipo e quando apagar e o service que o usa.
// Nunca e chamado dentro de uma transacao do banco (veja PlanoServiceImpl).
@ApplicationScoped
public class ArmazenamentoService {

    private static final Logger LOG = Logger.getLogger(ArmazenamentoService.class);
    static final String MENSAGEM_INDISPONIVEL = "O armazenamento de imagens está indisponível no momento. Tente novamente.";

    @Inject
    S3Client s3;

    @ConfigProperty(name = "fincontrol.storage.bucket")
    String bucket;

    public void enviar(String chave, byte[] conteudo, String contentType) {
        PutObjectRequest pedido = PutObjectRequest.builder()
                .bucket(bucket)
                .key(chave)
                .contentType(contentType)
                .contentLength((long) conteudo.length)
                .build();
        try {
            s3.putObject(pedido, RequestBody.fromBytes(conteudo));
        } catch (SdkException e) {
            throw indisponivel("enviar", chave, e);
        }
    }

    // a imagem tem no maximo 2 MB: ler tudo para a memoria e simples e nao deixa stream aberto
    public byte[] baixar(String chave) {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(chave).build()).asByteArray();
        } catch (NoSuchKeyException e) {
            throw objetoNaoEncontrado(chave);
        } catch (S3Exception e) {
            if (e.statusCode() == 404)
                throw objetoNaoEncontrado(chave);
            throw indisponivel("baixar", chave, e);
        } catch (SdkException e) {
            throw indisponivel("baixar", chave, e);
        }
    }

    public void excluir(String chave) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
        } catch (SdkException e) {
            throw indisponivel("apagar", chave, e);
        }
    }

    // para depois do commit: se falhar, o objeto fica orfao no bucket, mas a operacao do usuario ja valeu
    public void excluirSemFalhar(String chave) {
        if (chave == null)
            return;
        try {
            excluir(chave);
        } catch (RuntimeException e) {
            LOG.warnf(e, "Nao foi possivel apagar o objeto %s do bucket %s: ele ficou orfao.", chave, bucket);
        }
    }

    // volume apagado com a chave ainda no banco, por exemplo
    private ResourceNotFoundException objetoNaoEncontrado(String chave) {
        LOG.warnf("Objeto %s nao existe no bucket %s.", chave, bucket);
        return new ResourceNotFoundException("A imagem não foi encontrada no armazenamento.");
    }

    private ServicoExternoException indisponivel(String operacao, String chave, SdkException e) {
        LOG.errorf(e, "Falha ao %s o objeto %s no bucket %s.", operacao, chave, bucket);
        return new ServicoExternoException(MENSAGEM_INDISPONIVEL, e);
    }
}
