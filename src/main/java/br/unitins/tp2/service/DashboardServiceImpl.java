package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.DashboardResponseDTO;
import br.unitins.tp2.dto.DashboardResponseDTO.Cadastros;
import br.unitins.tp2.dto.DashboardResponseDTO.EvolucaoMensal;
import br.unitins.tp2.dto.DashboardResponseDTO.PlanoQuantidade;
import br.unitins.tp2.dto.DashboardResponseDTO.ReceitaMensal;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import br.unitins.tp2.util.IndicadoresAssinatura;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

// Carrega assinaturas, pagamentos e datas de cadastro e deixa as contas para IndicadoresAssinatura.
// Meses anteriores ao primeiro dado do sistema saem como "sem historico" (valores nulos, nunca zero).
@ApplicationScoped
public class DashboardServiceImpl implements DashboardService {

    private static final int MESES_PADRAO = 6;
    private static final int MESES_MAXIMO = 24;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PlanoRepository planoRepository;

    @Override
    public DashboardResponseDTO gerar(LocalDate inicio, LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        LocalDate fimPeriodo = fim != null ? fim : hoje;
        LocalDate inicioPeriodo = inicio != null ? inicio
                : fimPeriodo.withDayOfMonth(1).minusMonths(MESES_PADRAO - 1);
        validarPeriodo(inicioPeriodo, fimPeriodo, hoje);

        List<Assinatura> assinaturas = assinaturaRepository.listParaIndicadores();
        // um pagamento anual cobre ate 12 meses depois: busca a partir de um ano antes do inicio
        List<Pagamento> pagamentos = pagamentoRepository.findEntre(inicioPeriodo.minusYears(1), fimPeriodo);
        // o estorno conta no mes em que foi feito, mesmo de um pagamento de antes do periodo
        List<Pagamento> estornos = pagamentoRepository.findEstornadosEntre(inicioPeriodo, fimPeriodo);
        List<LocalDate> cadastros = usuarioRepository.findDatasCadastro(Papel.USER);
        LocalDate inicioHistorico = inicioDoHistorico(cadastros);
        List<Plano> planos = new ArrayList<>(planoRepository.listAll());
        planos.sort(Comparator.comparing(Plano::getPrecoMensal).thenComparing(Plano::getNome));

        List<ReceitaMensal> receitaPorMes = new ArrayList<>();
        List<EvolucaoMensal> evolucao = new ArrayList<>();
        for (YearMonth mes : IndicadoresAssinatura.meses(inicioPeriodo, fimPeriodo)) {
            boolean semHistorico = inicioHistorico == null || mes.isBefore(YearMonth.from(inicioHistorico));
            if (semHistorico) {
                receitaPorMes.add(new ReceitaMensal(mes.toString(), true, null, null, null));
                evolucao.add(new EvolucaoMensal(mes.toString(), true, null, List.of()));
                continue;
            }
            BigDecimal aprovado = IndicadoresAssinatura.receitaAprovadaDoMes(pagamentos, mes);
            BigDecimal estornado = IndicadoresAssinatura.estornadoNoMes(estornos, mes);
            receitaPorMes.add(new ReceitaMensal(mes.toString(), false, aprovado, estornado,
                    aprovado.subtract(estornado)));

            // o mes atual vai so ate hoje
            LocalDate referencia = mes.atEndOfMonth().isAfter(hoje) ? hoje : mes.atEndOfMonth();
            Map<Long, Long> pagantes = porIdDoPlano(IndicadoresAssinatura.pagantesPorPlano(pagamentos, referencia));
            List<PlanoQuantidade> porPlano = planos.stream()
                    .filter(plano -> !plano.isGratuito())
                    .map(plano -> toPlanoQuantidade(plano, pagantes.getOrDefault(plano.getId(), 0L)))
                    .toList();
            evolucao.add(new EvolucaoMensal(mes.toString(), false,
                    IndicadoresAssinatura.contasAte(cadastros, referencia), porPlano));
        }

        return new DashboardResponseDTO(
                inicioPeriodo,
                fimPeriodo,
                IndicadoresAssinatura.calcularMrr(assinaturas),
                IndicadoresAssinatura.calcularReceitaEmRisco(assinaturas),
                receitaPorMes.getLast(),
                receitaPorMes,
                assinantesPorPlano(assinaturas, planos),
                evolucao,
                cadastros(cadastros, inicioPeriodo, fimPeriodo));
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim, LocalDate hoje) {
        if (fim.isAfter(hoje))
            throw ValidationException.of("fim", "A data final não pode ser futura.");
        if (inicio.isAfter(fim))
            throw ValidationException.of("inicio", "A data inicial deve ser anterior ou igual à final.");
        if (IndicadoresAssinatura.meses(inicio, fim).size() > MESES_MAXIMO)
            throw ValidationException.of("inicio", "O período pode ter no máximo " + MESES_MAXIMO + " meses.");
    }

    // primeiro dado do sistema: o cadastro de conta USER ou o pagamento mais antigo
    private LocalDate inicioDoHistorico(List<LocalDate> cadastros) {
        LocalDate primeiroCadastro = cadastros.stream().min(Comparator.naturalOrder()).orElse(null);
        LocalDate primeiroPagamento = pagamentoRepository.findPrimeiraData();
        if (primeiroCadastro == null)
            return primeiroPagamento;
        if (primeiroPagamento == null)
            return primeiroCadastro;
        return primeiroCadastro.isBefore(primeiroPagamento) ? primeiroCadastro : primeiroPagamento;
    }

    // hoje: contas ativas por plano vigente (todos os planos, mesmo sem assinante)
    private List<PlanoQuantidade> assinantesPorPlano(List<Assinatura> assinaturas, List<Plano> planos) {
        Map<Long, Long> porPlano = new HashMap<>();
        for (Assinatura assinatura : assinaturas) {
            if (Boolean.TRUE.equals(assinatura.getUsuario().getAtivo()))
                porPlano.merge(assinatura.getPlano().getId(), 1L, Long::sum);
        }
        return planos.stream()
                .filter(plano -> plano.getAtivo() || porPlano.containsKey(plano.getId()))
                .map(plano -> toPlanoQuantidade(plano, porPlano.getOrDefault(plano.getId(), 0L)))
                .toList();
    }

    private static Map<Long, Long> porIdDoPlano(Map<Plano, Long> porPlano) {
        Map<Long, Long> porId = new HashMap<>();
        porPlano.forEach((plano, quantidade) -> porId.merge(plano.getId(), quantidade, Long::sum));
        return porId;
    }

    private Cadastros cadastros(List<LocalDate> cadastros, LocalDate inicio, LocalDate fim) {
        long novos = cadastros.stream().filter(data -> !data.isBefore(inicio) && !data.isAfter(fim)).count();
        long convertidos = usuarioRepository.countCadastradosComPagamentoAprovado(Papel.USER,
                inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        return new Cadastros(novos, convertidos, IndicadoresAssinatura.taxaConversao(novos, convertidos));
    }

    private static PlanoQuantidade toPlanoQuantidade(Plano plano, long quantidade) {
        return new PlanoQuantidade(plano.getId(), plano.getNome(), plano.isGratuito(), quantidade);
    }
}
