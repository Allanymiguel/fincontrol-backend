package br.unitins.tp2.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

// O que o administrador fez. Codigos explicitos e estaveis: novas acoes entram no fim.
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AcaoAuditoria implements EnumComId {
    CRIACAO_USUARIO(1L, "Criação de usuário"),
    EDICAO_USUARIO(2L, "Edição de usuário"),
    ATIVACAO_CONTA(3L, "Ativação de conta"),
    DESATIVACAO_CONTA(4L, "Desativação de conta"),
    TROCA_PLANO(5L, "Troca de plano"),
    DESFAZER_TROCA(6L, "Troca agendada desfeita"),
    CANCELAMENTO_PLANO(7L, "Cancelamento de plano"),
    OBSERVACAO_ASSINATURA(8L, "Observação da assinatura"),
    CRIACAO_PLANO(9L, "Criação de plano"),
    ALTERACAO_PLANO(10L, "Alteração de plano"),
    EXCLUSAO_PLANO(11L, "Exclusão de plano"),
    CRIACAO_CUPOM(12L, "Criação de cupom"),
    ALTERACAO_CUPOM(13L, "Alteração de cupom"),
    EXCLUSAO_CUPOM(14L, "Exclusão de cupom"),
    CRIACAO_PARAMETRO_FISCAL(15L, "Criação de parâmetro fiscal"),
    ALTERACAO_PARAMETRO_FISCAL(16L, "Alteração de parâmetro fiscal"),
    EXCLUSAO_PARAMETRO_FISCAL(17L, "Exclusão de parâmetro fiscal"),
    CRIACAO_FAIXA_IRPF(18L, "Criação de faixa do IRPF"),
    ALTERACAO_FAIXA_IRPF(19L, "Alteração de faixa do IRPF"),
    EXCLUSAO_FAIXA_IRPF(20L, "Exclusão de faixa do IRPF"),
    CORRECAO_CEP(21L, "Correção de CEP"),
    VERIFICACAO_CEP(22L, "Verificação de CEP"),
    REVALIDACAO_CEP(23L, "Revalidação de CEP"),
    IMPORTACAO_CEP(24L, "Importação de CEPs"),
    ALTERACAO_API_CEP(25L, "Consulta externa de CEP ligada/desligada"),
    COBRANCA_MANUAL(26L, "Cobrança disparada manualmente"),
    ENCERRAMENTO_SESSOES(27L, "Sessões encerradas"),
    ESTORNO_PAGAMENTO(28L, "Estorno de pagamento"),
    SIMULACAO_RECUSA(29L, "Simulação de recusa de pagamento"),
    ENVIO_IMAGEM_PLANO(30L, "Envio de imagem do plano"),
    REMOCAO_IMAGEM_PLANO(31L, "Remoção de imagem do plano");

    private final Long ID;
    private final String NOME;

    AcaoAuditoria(Long id, String nome) {
        this.ID = id;
        this.NOME = nome;
    }

    @Override
    public Long getId() {
        return ID;
    }

    public String getNome() {
        return NOME;
    }

    public static AcaoAuditoria valueOf(Long id) {
        if (id == null)
            return null;

        for (AcaoAuditoria a : AcaoAuditoria.values()) {
            if (Objects.equals(a.getId(), id))
                return a;
        }
        return null;
    }
}
