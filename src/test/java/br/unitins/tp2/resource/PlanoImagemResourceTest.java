package br.unitins.tp2.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.service.ArmazenamentoService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;

// GET publico da imagem do plano: cabecalhos de cache, download e erros (S3 simulado)
@QuarkusTest
class PlanoImagemResourceTest {

    static final byte[] PNG = Arrays.copyOf(new byte[] { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A }, 32);

    @Inject
    PlanoRepository planoRepository;

    @InjectMock
    ArmazenamentoService armazenamento;

    @Test
    void semTokenDevolveAImagemComOsCabecalhos() {
        Plano plano = planoComImagem("promoção.png");
        String versao = versao(plano);
        when(armazenamento.baixar(plano.getImagem().getChave())).thenReturn(PNG);

        Response resposta = given().when().get("/planos/{id}/imagem", plano.getId());

        resposta.then().statusCode(200)
                .contentType("image/png")
                .header("Content-Length", String.valueOf(PNG.length))
                .header("ETag", "\"" + versao + "\"")
                .header("Cache-Control", "no-cache")
                .header("Content-Disposition",
                        "inline; filename=\"promocao.png\"; filename*=UTF-8''promo%C3%A7%C3%A3o.png")
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox");
        assertArrayEquals(PNG, resposta.asByteArray());
    }

    @Test
    void comAVersaoAtualFicaEmCacheEComVersaoAntigaRevalida() {
        Plano plano = planoComImagem("logo.png");
        when(armazenamento.baixar(anyString())).thenReturn(PNG);

        given().queryParam("v", versao(plano)).when().get("/planos/{id}/imagem", plano.getId())
                .then().statusCode(200).header("Cache-Control", "public, max-age=31536000, immutable");
        given().queryParam("v", "versao-antiga").when().get("/planos/{id}/imagem", plano.getId())
                .then().statusCode(200).header("Cache-Control", "no-cache");
    }

    @Test
    void ifNoneMatchDaVersaoAtualResponde304SemChamarOS3() {
        Plano plano = planoComImagem("logo.png");
        String etag = "\"" + versao(plano) + "\"";

        Response resposta = given().header("If-None-Match", etag).when().get("/planos/{id}/imagem", plano.getId());

        resposta.then().statusCode(304).header("ETag", etag).header("Cache-Control", "no-cache");
        assertEquals(1, resposta.headers().getValues("ETag").size(), "um ETag so");

        verify(armazenamento, never()).baixar(anyString());
    }

    @Test
    void downloadRespondeComoAnexo() {
        Plano plano = planoComImagem("logo.png");
        when(armazenamento.baixar(anyString())).thenReturn(PNG);

        given().queryParam("download", true).when().get("/planos/{id}/imagem", plano.getId())
                .then().statusCode(200)
                .header("Content-Disposition", "attachment; filename=\"logo.png\"; filename*=UTF-8''logo.png");
    }

    @Test
    void planoInexistenteOuSemImagemDa404ProblemSemCache() {
        given().header("Accept", "image/*").when().get("/planos/{id}/imagem", 999_999)
                .then().statusCode(404).contentType("application/problem+json")
                .header("Cache-Control", "no-store").body("detail", equalTo("Plano não encontrado."));

        Plano semImagem = salvar(novoPlano(null));
        given().header("Accept", "image/*").when().get("/planos/{id}/imagem", semImagem.getId())
                .then().statusCode(404).contentType("application/problem+json")
                .body("detail", equalTo("Este plano não tem imagem."));
    }

    @Test
    void objetoQueSumiuDoBucketDa404() {
        Plano plano = planoComImagem("logo.png");
        when(armazenamento.baixar(anyString()))
                .thenThrow(new ResourceNotFoundException("A imagem não foi encontrada no armazenamento."));

        given().header("Accept", "image/*").when().get("/planos/{id}/imagem", plano.getId())
                .then().statusCode(404).contentType("application/problem+json").header("Cache-Control", "no-store");
    }

    @Test
    void armazenamentoForaDoArDa503SemCache() {
        Plano plano = planoComImagem("logo.png");
        when(armazenamento.baixar(anyString())).thenThrow(new ServicoExternoException("indisponivel", null));

        given().header("Accept", "image/*").when().get("/planos/{id}/imagem", plano.getId())
                .then().statusCode(503).contentType("application/problem+json").header("Cache-Control", "no-store");
    }

    private Plano planoComImagem(String nome) {
        String chave = "planos/0/" + UUID.randomUUID() + ".png";
        return salvar(novoPlano(new ArquivoArmazenado(chave, nome, "image/png", PNG.length, LocalDateTime.now())));
    }

    private Plano salvar(Plano plano) {
        QuarkusTransaction.requiringNew().run(() -> planoRepository.persist(plano));
        return plano;
    }

    private static String versao(Plano plano) {
        String chave = plano.getImagem().getChave();
        return chave.substring(chave.lastIndexOf('/') + 1, chave.lastIndexOf('.'));
    }

    static Plano novoPlano(ArquivoArmazenado imagem) {
        Plano plano = new Plano();
        plano.setNome("Teste " + UUID.randomUUID().toString().substring(0, 8));
        plano.setPrecoMensal(new BigDecimal("9.90"));
        plano.setPrecoAnual(new BigDecimal("99.00"));
        plano.setLimiteTransacoes(100);
        plano.setLimiteMetas(1);
        plano.setLiberaRecorrencias(false);
        plano.setLiberaFiscal(false);
        plano.setLiberaExportacao(false);
        plano.setAtivo(true);
        plano.setImagem(imagem);
        return plano;
    }
}
