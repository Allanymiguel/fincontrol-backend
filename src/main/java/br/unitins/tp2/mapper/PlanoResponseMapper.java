package br.unitins.tp2.mapper;

import br.unitins.tp2.dto.ImagemPlanoResponseDTO;
import br.unitins.tp2.dto.PlanoCatalogoResponseDTO;
import br.unitins.tp2.dto.PlanoResponseDTO;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.util.RegrasImagem;

public final class PlanoResponseMapper {

    private PlanoResponseMapper() {
    }

    public static PlanoResponseDTO toResponse(Plano plano) {
        return new PlanoResponseDTO(
                plano.getId(),
                plano.getNome(),
                plano.getPrecoMensal(),
                plano.getPrecoAnual(),
                plano.getLimiteTransacoes(),
                plano.getLimiteMetas(),
                plano.getLiberaRecorrencias(),
                plano.getLiberaFiscal(),
                plano.getLiberaExportacao(),
                plano.getAtivo(),
                imagem(plano.getImagem()));
    }

    public static PlanoCatalogoResponseDTO toCatalogo(Plano plano, Long idPlanoAtual) {
        return new PlanoCatalogoResponseDTO(toResponse(plano), plano.getId().equals(idPlanoAtual));
    }

    // a chave do S3 nao sai daqui: o front recebe so a versao (o uuid da chave)
    private static ImagemPlanoResponseDTO imagem(ArquivoArmazenado imagem) {
        if (imagem == null)
            return null;
        return new ImagemPlanoResponseDTO(
                imagem.getNome(),
                imagem.getTipo(),
                imagem.getTamanho(),
                imagem.getEnviadoEm(),
                RegrasImagem.versao(imagem.getChave()));
    }
}
