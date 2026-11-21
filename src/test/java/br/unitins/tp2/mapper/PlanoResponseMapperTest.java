package br.unitins.tp2.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.unitins.tp2.dto.ImagemPlanoResponseDTO;
import br.unitins.tp2.dto.PlanoResponseDTO;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Plano;

// A imagem do plano na resposta: metadados e versao, nunca a chave do S3
class PlanoResponseMapperTest {

    private static final String CHAVE = "planos/2/3f2a1c9e-8b7d-4e6f-9a01-23456789abcd.png";

    @Test
    void semImagemARespostaTemImagemNula() {
        assertNull(PlanoResponseMapper.toResponse(plano(null)).imagem());
    }

    @Test
    void comImagemExpoeOsMetadadosEAVersaoDaChave() {
        LocalDateTime enviadaEm = LocalDateTime.of(2026, 11, 21, 14, 3, 27);
        ArquivoArmazenado arquivo = new ArquivoArmazenado(CHAVE, "logo.png", "image/png", 512 * 1024, enviadaEm);

        ImagemPlanoResponseDTO imagem = PlanoResponseMapper.toResponse(plano(arquivo)).imagem();

        assertEquals("logo.png", imagem.nome());
        assertEquals("image/png", imagem.tipo());
        assertEquals(524288L, imagem.tamanho());
        assertEquals(enviadaEm, imagem.enviadaEm());
        assertEquals("3f2a1c9e-8b7d-4e6f-9a01-23456789abcd", imagem.versao());
    }

    @Test
    void oJsonNaoTemAChaveDoS3() throws Exception {
        ArquivoArmazenado arquivo = new ArquivoArmazenado(CHAVE, "logo.png", "image/png", 10, LocalDateTime.now());
        PlanoResponseDTO resposta = PlanoResponseMapper.toResponse(plano(arquivo));

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(resposta);

        assertTrue(json.contains("\"versao\":\"3f2a1c9e-8b7d-4e6f-9a01-23456789abcd\""));
        assertFalse(json.contains("planos/2/"));
        assertFalse(json.contains("chave"));
    }

    private static Plano plano(ArquivoArmazenado imagem) {
        Plano plano = new Plano();
        plano.setNome("Empreendedor");
        plano.setPrecoMensal(new BigDecimal("39.90"));
        plano.setPrecoAnual(new BigDecimal("399.00"));
        plano.setLimiteTransacoes(10000);
        plano.setLimiteMetas(50);
        plano.setLiberaRecorrencias(true);
        plano.setLiberaFiscal(true);
        plano.setLiberaExportacao(true);
        plano.setAtivo(true);
        plano.setImagem(imagem);
        return plano;
    }
}
