package br.unitins.tp2.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.FaixaIrpf;
import br.unitins.tp2.model.ParametroFiscal;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.Usuario;

// Retrato de uma entidade para a auditoria: rotulo do campo -> valor ja formatado para leitura.
// Comparando o retrato de antes com o de depois, AlteracoesAuditoria acha o que mudou.
// So entram dados da relacao com o FinControl; documento e telefone entram so para saber SE
// mudaram (AlteracoesAuditoria nunca grava o valor deles).
public final class RetratoAuditoria {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RetratoAuditoria() {
    }

    public static Map<String, String> de(Plano plano) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Nome", plano.getNome());
        r.put("Preço mensal", numero(plano.getPrecoMensal()));
        r.put("Preço anual", numero(plano.getPrecoAnual()));
        r.put("Limite de transações", inteiro(plano.getLimiteTransacoes()));
        r.put("Limite de metas", inteiro(plano.getLimiteMetas()));
        r.put("Recorrências", simNao(plano.getLiberaRecorrencias()));
        r.put("Módulo fiscal", simNao(plano.getLiberaFiscal()));
        r.put("Exportação", simNao(plano.getLiberaExportacao()));
        r.put("Ativo", simNao(plano.getAtivo()));
        return r;
    }

    public static Map<String, String> de(Cupom cupom) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Código", cupom.getCodigo());
        r.put("Desconto (%)", numero(cupom.getPercentualDesconto()));
        r.put("Validade", data(cupom.getValidade()));
        r.put("Ativo", simNao(cupom.getAtivo()));
        return r;
    }

    public static Map<String, String> de(ParametroFiscal parametro) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Chave", parametro.getChave());
        r.put("Valor", numero(parametro.getValor()));
        r.put("Início da vigência", data(parametro.getVigenciaInicio()));
        r.put("Fim da vigência", data(parametro.getVigenciaFim()));
        return r;
    }

    public static Map<String, String> de(FaixaIrpf faixa) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Vigência", data(faixa.getVigenciaInicio()));
        r.put("Limite inferior", numero(faixa.getLimiteInferior()));
        r.put("Limite superior", numero(faixa.getLimiteSuperior()));
        r.put("Alíquota (%)", numero(faixa.getAliquota()));
        r.put("Parcela a deduzir", numero(faixa.getParcelaDeduzir()));
        return r;
    }

    public static Map<String, String> de(Usuario usuario) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Nome", usuario.getNome());
        r.put("E-mail", usuario.getEmail());
        r.put("Papel", usuario.getPapel() == null ? null : usuario.getPapel().getNome());
        r.put("Ativo", simNao(usuario.getAtivo()));
        r.put(AlteracoesAuditoria.DOCUMENTO, usuario.getDocumento());
        r.put(AlteracoesAuditoria.TELEFONE, usuario.getTelefone());
        r.put("Perfil fiscal", usuario.getPerfilFiscal() == null ? null : usuario.getPerfilFiscal().getNome());
        r.put("Atividade MEI", usuario.getAtividadeMei() == null ? null : usuario.getAtividadeMei().getNome());
        Endereco endereco = usuario.getEndereco();
        r.put("CEP", endereco == null ? null : endereco.getCep());
        r.put("Logradouro", endereco == null ? null : endereco.getLogradouro());
        r.put("Número", endereco == null ? null : endereco.getNumero());
        r.put("Complemento", endereco == null ? null : endereco.getComplemento());
        r.put("Bairro", endereco == null ? null : endereco.getBairro());
        r.put("Município", endereco == null || endereco.getMunicipio() == null ? null
                : endereco.getMunicipio().getNome() + " - " + endereco.getMunicipio().getEstado().getSigla());
        return r;
    }

    public static Map<String, String> de(Assinatura assinatura) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("Plano", assinatura.getPlano().getNome());
        r.put("Periodicidade", assinatura.getPeriodicidade().getNome());
        r.put("Cupom", assinatura.getCupom() == null ? null : assinatura.getCupom().getCodigo());
        r.put("Forma de pagamento", assinatura.getFormaPagamento().getNome());
        r.put("Valor da cobrança", numero(assinatura.getValor()));
        r.put("Próxima cobrança", data(assinatura.getDataProximaCobranca()));
        r.put("Troca agendada", assinatura.isTrocaAgendada()
                ? assinatura.getProximoPlano().getNome() + " (" + assinatura.getProximaPeriodicidade().getNome() + ")"
                : null);
        r.put("Observação", assinatura.getObservacao());
        return r;
    }

    // "Nome (e-mail)": como o usuario aparece na lista da auditoria (sem documento nem telefone)
    public static String descricao(Usuario usuario) {
        return usuario.getNome() + " (" + usuario.getEmail() + ")";
    }

    public static String descricao(ParametroFiscal parametro) {
        return parametro.getChave() + " (desde " + data(parametro.getVigenciaInicio()) + ")";
    }

    public static String descricao(FaixaIrpf faixa) {
        return "Tabela de " + data(faixa.getVigenciaInicio()) + ", a partir de R$ " + numero(faixa.getLimiteInferior());
    }

    private static String numero(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private static String inteiro(Integer valor) {
        return valor == null ? null : valor.toString();
    }

    private static String data(LocalDate data) {
        return data == null ? null : data.format(DATA_BR);
    }

    private static String simNao(Boolean valor) {
        return valor == null ? null : (valor ? "Sim" : "Não");
    }
}
