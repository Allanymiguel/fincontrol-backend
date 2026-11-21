package br.unitins.tp2.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

// Na subida, garante que o bucket das imagens existe (cria se faltar). Com o S3 fora do ar so avisa no
// log e o app sobe mesmo assim: as telas que nao usam imagens continuam funcionando, e enviar ou ler uma
// imagem responde 503 ate o armazenamento voltar. Desligado nos testes (verificar-bucket-na-subida).
@ApplicationScoped
public class VerificacaoBucket {

    private static final Logger LOG = Logger.getLogger(VerificacaoBucket.class);

    @Inject
    S3Client s3;

    @ConfigProperty(name = "fincontrol.storage.bucket")
    String bucket;

    @ConfigProperty(name = "fincontrol.storage.verificar-bucket-na-subida", defaultValue = "true")
    boolean verificarNaSubida;

    void aoSubir(@Observes StartupEvent evento) {
        if (verificarNaSubida)
            verificar();
    }

    void verificar() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            LOG.infof("Armazenamento S3: bucket %s pronto.", bucket);
        } catch (NoSuchBucketException e) {
            criar();
        } catch (S3Exception e) {
            if (e.statusCode() == 404)
                criar();
            else
                avisar(e);
        } catch (SdkException e) {
            avisar(e);
        }
    }

    private void criar() {
        try {
            s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            LOG.infof("Armazenamento S3: bucket %s criado e pronto.", bucket);
        } catch (SdkException e) {
            avisar(e);
        }
    }

    private void avisar(SdkException e) {
        LOG.warnf("Armazenamento S3 indisponivel na subida (bucket %s): %s. O app sobe mesmo assim; "
                + "as imagens dos planos respondem 503 ate o armazenamento voltar.", bucket, e.getMessage());
    }
}
