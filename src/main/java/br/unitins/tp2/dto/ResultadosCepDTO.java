package br.unitins.tp2.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.ResultadoConsultaCep;

// Respostas das acoes de gestao da base de CEPs
public final class ResultadosCepDTO {

    private ResultadosCepDTO() {
    }

    // revalidacao em lote dos CEPs manuais nao verificados
    public record RevalidacaoLote(int conferidos, int verificados, int divergentes, int falhas) {
    }

    // importacao CSV: cada linha rejeitada traz o numero da linha no arquivo e o motivo
    public record Importacao(int importados, int atualizados, List<Rejeicao> rejeitados) {

        public record Rejeicao(int linha, String cep, String motivo) {
        }
    }

    public record Configuracao(boolean apiExternaHabilitada) {
    }

    public record Consulta(Long id, String cep, LocalDateTime dataHora, FonteConsultaCep fonte,
            ResultadoConsultaCep resultado) {
    }
}
