package br.unitins.tp2.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.AtividadeMei;
import br.unitins.tp2.model.EnumComId;
import br.unitins.tp2.model.EscopoTransacao;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import br.unitins.tp2.model.SituacaoCep;
import br.unitins.tp2.model.OrigemMudancaAssinatura;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.PerfilFiscal;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.model.TipoContato;
import br.unitins.tp2.model.TipoMudancaAssinatura;
import br.unitins.tp2.model.TipoTransacao;

// Os enums vao para o banco pelo codigo explicito (getId), nunca pelo nome nem pelo ordinal.
// Estes testes travam os codigos: renumerar uma constante quebraria os dados ja gravados.
class EnumIdConverterTest {

    // todos os converters da aplicacao (um por enum persistido)
    private static final List<EnumIdConverter<?>> CONVERTERS = List.of(
            new AtividadeMeiConverter(), new EscopoTransacaoConverter(), new FormaPagamentoConverter(),
            new PapelConverter(), new PerfilFiscalConverter(), new PeriodicidadeConverter(),
            new StatusAssinaturaConverter(), new StatusPagamentoConverter(), new TipoContatoConverter(),
            new TipoTransacaoConverter(), new TipoMudancaAssinaturaConverter(),
            new OrigemMudancaAssinaturaConverter(), new AcaoAuditoriaConverter(), new TipoAlvoAuditoriaConverter(),
            new OrigemCepConverter(), new SituacaoCepConverter(), new FonteConsultaCepConverter(),
            new ResultadoConsultaCepConverter());

    // codigo esperado de cada constante: o contrato com o banco (import.sql) e com a API (idXxx)
    private static final Map<Enum<?>, Integer> CODIGOS = new LinkedHashMap<>();

    static {
        CODIGOS.put(AtividadeMei.COMERCIO, 1);
        CODIGOS.put(AtividadeMei.SERVICO, 2);
        CODIGOS.put(AtividadeMei.AMBOS, 3);
        CODIGOS.put(EscopoTransacao.PESSOAL, 1);
        CODIGOS.put(EscopoTransacao.EMPRESA, 2);
        CODIGOS.put(FormaPagamento.PIX, 1);
        CODIGOS.put(FormaPagamento.CARTAO, 2);
        CODIGOS.put(FormaPagamento.BOLETO, 3);
        CODIGOS.put(FormaPagamento.NENHUMA, 4);
        CODIGOS.put(Papel.USER, 1);
        CODIGOS.put(Papel.ADMIN, 2);
        CODIGOS.put(PerfilFiscal.PF, 1);
        CODIGOS.put(PerfilFiscal.MEI, 2);
        CODIGOS.put(PerfilFiscal.ME, 3);
        CODIGOS.put(Periodicidade.MENSAL, 1);
        CODIGOS.put(Periodicidade.ANUAL, 2);
        CODIGOS.put(StatusAssinatura.PENDENTE, 1);
        CODIGOS.put(StatusAssinatura.ATIVA, 2);
        CODIGOS.put(StatusAssinatura.CANCELADA, 3);
        CODIGOS.put(StatusPagamento.PENDENTE, 1);
        CODIGOS.put(StatusPagamento.APROVADO, 2);
        CODIGOS.put(StatusPagamento.RECUSADO, 3);
        CODIGOS.put(TipoContato.CLIENTE, 1);
        CODIGOS.put(TipoContato.FORNECEDOR, 2);
        CODIGOS.put(TipoTransacao.RECEITA, 1);
        CODIGOS.put(TipoTransacao.DESPESA, 2);
        CODIGOS.put(TipoMudancaAssinatura.CADASTRO, 1);
        CODIGOS.put(TipoMudancaAssinatura.UPGRADE, 2);
        CODIGOS.put(TipoMudancaAssinatura.TROCA_IMEDIATA, 3);
        CODIGOS.put(TipoMudancaAssinatura.DOWNGRADE_AGENDADO, 4);
        CODIGOS.put(TipoMudancaAssinatura.CANCELAMENTO_AGENDADO, 5);
        CODIGOS.put(TipoMudancaAssinatura.AGENDAMENTO_DESFEITO, 6);
        CODIGOS.put(TipoMudancaAssinatura.DOWNGRADE_APLICADO, 7);
        CODIGOS.put(TipoMudancaAssinatura.CANCELAMENTO_APLICADO, 8);
        CODIGOS.put(TipoMudancaAssinatura.VOLTA_GRATUITO_CONTA_DESATIVADA, 9);
        CODIGOS.put(OrigemMudancaAssinatura.CADASTRO, 1);
        CODIGOS.put(OrigemMudancaAssinatura.ADMIN, 2);
        CODIGOS.put(OrigemMudancaAssinatura.COBRANCA, 3);
        AcaoAuditoria[] acoes = { AcaoAuditoria.CRIACAO_USUARIO, AcaoAuditoria.EDICAO_USUARIO,
                AcaoAuditoria.ATIVACAO_CONTA, AcaoAuditoria.DESATIVACAO_CONTA, AcaoAuditoria.TROCA_PLANO,
                AcaoAuditoria.DESFAZER_TROCA, AcaoAuditoria.CANCELAMENTO_PLANO, AcaoAuditoria.OBSERVACAO_ASSINATURA,
                AcaoAuditoria.CRIACAO_PLANO, AcaoAuditoria.ALTERACAO_PLANO, AcaoAuditoria.EXCLUSAO_PLANO,
                AcaoAuditoria.CRIACAO_CUPOM, AcaoAuditoria.ALTERACAO_CUPOM, AcaoAuditoria.EXCLUSAO_CUPOM,
                AcaoAuditoria.CRIACAO_PARAMETRO_FISCAL, AcaoAuditoria.ALTERACAO_PARAMETRO_FISCAL,
                AcaoAuditoria.EXCLUSAO_PARAMETRO_FISCAL, AcaoAuditoria.CRIACAO_FAIXA_IRPF,
                AcaoAuditoria.ALTERACAO_FAIXA_IRPF, AcaoAuditoria.EXCLUSAO_FAIXA_IRPF, AcaoAuditoria.CORRECAO_CEP,
                AcaoAuditoria.VERIFICACAO_CEP, AcaoAuditoria.REVALIDACAO_CEP, AcaoAuditoria.IMPORTACAO_CEP,
                AcaoAuditoria.ALTERACAO_API_CEP };
        for (int i = 0; i < acoes.length; i++)
            CODIGOS.put(acoes[i], i + 1);
        CODIGOS.put(TipoAlvoAuditoria.USUARIO, 1);
        CODIGOS.put(TipoAlvoAuditoria.ASSINATURA, 2);
        CODIGOS.put(TipoAlvoAuditoria.PLANO, 3);
        CODIGOS.put(TipoAlvoAuditoria.CUPOM, 4);
        CODIGOS.put(TipoAlvoAuditoria.PARAMETRO_FISCAL, 5);
        CODIGOS.put(TipoAlvoAuditoria.FAIXA_IRPF, 6);
        CODIGOS.put(TipoAlvoAuditoria.CEP, 7);
        CODIGOS.put(TipoAlvoAuditoria.CONFIGURACAO, 8);
        CODIGOS.put(OrigemCep.API, 1);
        CODIGOS.put(OrigemCep.MANUAL, 2);
        CODIGOS.put(OrigemCep.IMPORTACAO, 3);
        CODIGOS.put(SituacaoCep.VERIFICADO, 1);
        CODIGOS.put(SituacaoCep.NAO_VERIFICADO, 2);
        CODIGOS.put(SituacaoCep.DIVERGENTE, 3);
        CODIGOS.put(FonteConsultaCep.BASE_LOCAL, 1);
        CODIGOS.put(FonteConsultaCep.VIACEP, 2);
        CODIGOS.put(ResultadoConsultaCep.ENCONTRADO, 1);
        CODIGOS.put(ResultadoConsultaCep.NAO_ENCONTRADO, 2);
        CODIGOS.put(ResultadoConsultaCep.FALHA, 3);
    }

    // enum so de teste com codigos fora da ordem das constantes
    enum ForaDeOrdem implements EnumComId {
        B(5L), A(10L), C(1L);

        private final Long id;

        ForaDeOrdem(Long id) {
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }
    }

    static class ForaDeOrdemConverter extends EnumIdConverter<ForaDeOrdem> {
        ForaDeOrdemConverter() {
            super(ForaDeOrdem.class);
        }
    }

    @Test
    void cadaConstanteTemOCodigoEsperado() {
        CODIGOS.forEach((constante, codigo) -> assertEquals(codigo, codigoNoBanco(constante),
                constante.getDeclaringClass().getSimpleName() + "." + constante.name()));
    }

    @Test
    void todasAsConstantesTemCodigoTravadoNoTeste() {
        for (EnumIdConverter<?> converter : CONVERTERS) {
            for (Object constante : tipoDo(converter).getEnumConstants())
                assertTrue(CODIGOS.containsKey(constante), "sem codigo esperado: " + constante);
        }
    }

    @Test
    void idaEVoltaPeloCodigo() {
        CODIGOS.keySet().forEach((constante) -> assertEquals(constante, lerDoBanco(constante, codigoNoBanco(constante))));
    }

    @Test
    void codigosSaoUnicosEmCadaEnum() {
        for (EnumIdConverter<?> converter : CONVERTERS) {
            Set<Long> vistos = new HashSet<>();
            for (Object constante : tipoDo(converter).getEnumConstants())
                assertTrue(vistos.add(((EnumComId) constante).getId()), "codigo repetido em " + constante);
        }
    }

    // a conversao do banco e o valueOf(Long) usado na API (campos idXxx) concordam
    @Test
    void valueOfDaApiConcordaComOConverter() throws Exception {
        for (EnumIdConverter<?> converter : CONVERTERS) {
            Class<?> tipo = tipoDo(converter);
            Method valueOf = tipo.getMethod("valueOf", Long.class);
            for (Object constante : tipo.getEnumConstants()) {
                Long id = ((EnumComId) constante).getId();
                assertEquals(constante, valueOf.invoke(null, id));
                assertEquals(constante, lerDoBanco((Enum<?>) constante, id.intValue()));
            }
            assertNull(valueOf.invoke(null, 999L), tipo.getSimpleName() + ".valueOf(999)");
            assertNull(valueOf.invoke(null, (Long) null), tipo.getSimpleName() + ".valueOf(null)");
        }
    }

    @Test
    void naoUsaOrdinal() {
        ForaDeOrdemConverter converter = new ForaDeOrdemConverter();
        assertEquals(5, converter.convertToDatabaseColumn(ForaDeOrdem.B));
        assertEquals(10, converter.convertToDatabaseColumn(ForaDeOrdem.A));
        assertEquals(1, converter.convertToDatabaseColumn(ForaDeOrdem.C));
        assertEquals(ForaDeOrdem.A, converter.convertToEntityAttribute(10));
        assertEquals(ForaDeOrdem.C, converter.convertToEntityAttribute(1));
        // 0 seria o ordinal de B: nao existe como codigo
        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute(0));
    }

    @Test
    void codigoDesconhecidoNoBancoFalha() {
        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> new TipoTransacaoConverter().convertToEntityAttribute(99));
        assertTrue(erro.getMessage().contains("TipoTransacao"));
    }

    @Test
    void nuloContinuaNulo() {
        TipoTransacaoConverter converter = new TipoTransacaoConverter();
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
    }

    // ===== auxiliares: o converter de cada constante e escolhido pelo tipo do enum =====

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Integer codigoNoBanco(Enum<?> constante) {
        return ((EnumIdConverter) converterDe(constante)).convertToDatabaseColumn(constante);
    }

    private static Object lerDoBanco(Enum<?> constante, Integer codigo) {
        return converterDe(constante).convertToEntityAttribute(codigo);
    }

    private static EnumIdConverter<?> converterDe(Enum<?> constante) {
        return CONVERTERS.stream()
                .filter((c) -> tipoDo(c) == constante.getDeclaringClass())
                .findFirst()
                .orElseThrow(() -> new AssertionError("sem converter para " + constante.getDeclaringClass()));
    }

    private static Class<?> tipoDo(EnumIdConverter<?> converter) {
        return converter.tipo();
    }
}
