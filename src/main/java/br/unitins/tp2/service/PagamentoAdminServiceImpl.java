package br.unitins.tp2.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.EstornoPagamentoDTO;
import br.unitins.tp2.dto.PagamentoResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.Pagamento;
import br.unitins.tp2.model.StatusPagamento;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.util.RegrasCobranca;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PagamentoAdminServiceImpl implements PagamentoAdminService {

    @Inject
    PagamentoRepository repository;

    @Inject
    AuditoriaService auditoria;

    @Override
    public List<Pagamento> buscar(Filtro filtro, int page, int pageSize) {
        validarPeriodo(filtro);
        return repository.findByFiltros(filtro.inicio(), filtro.fim(), filtro.idPlano(), filtro.forma(),
                filtro.status(), filtro.busca()).page(page, pageSize).list();
    }

    @Override
    public long contar(Filtro filtro) {
        return repository.findByFiltros(filtro.inicio(), filtro.fim(), filtro.idPlano(), filtro.forma(),
                filtro.status(), filtro.busca()).count();
    }

    @Override
    public List<PagamentoResponseDTO.TotalPorStatus> totais(Filtro filtro) {
        validarPeriodo(filtro);
        return repository.totaisPorStatus(filtro.inicio(), filtro.fim(), filtro.idPlano(), filtro.forma(),
                filtro.busca()).stream()
                .map(t -> new PagamentoResponseDTO.TotalPorStatus(t.status(), t.quantidade(), t.valor()))
                .toList();
    }

    @Override
    public Pagamento findById(long id) {
        Pagamento pagamento = repository.findById(id);
        if (pagamento == null)
            throw new ResourceNotFoundException("Pagamento não encontrado.");
        return pagamento;
    }

    // Estorno simulado (sem gateway, RN15): marca o pagamento, guarda motivo, data e admin e audita.
    // A receita do painel desconta o valor no mes do estorno; plano e periodo da assinatura nao mudam.
    @Override
    @Transactional
    public Pagamento estornar(long id, EstornoPagamentoDTO estorno) {
        Pagamento pagamento = findById(id);
        String impedimento = RegrasCobranca.motivoParaNaoEstornar(pagamento.getStatus());
        if (impedimento != null)
            throw ValidationException.of("status", impedimento);

        Map<String, String> antes = RetratoAuditoria.de(pagamento);
        AuditoriaService.Admin admin = auditoria.adminLogado();
        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamento.setDataEstorno(LocalDateTime.now());
        pagamento.setMotivoEstorno(estorno.motivo().trim());
        pagamento.setEstornadoPor(admin.nome() + (admin.email() == null ? "" : " (" + admin.email() + ")"));
        auditoria.registrar(AcaoAuditoria.ESTORNO_PAGAMENTO, TipoAlvoAuditoria.PAGAMENTO, pagamento.getId(),
                RetratoAuditoria.descricao(pagamento), antes, RetratoAuditoria.de(pagamento));
        return pagamento;
    }

    private static void validarPeriodo(Filtro filtro) {
        if (filtro.inicio() != null && filtro.fim() != null && filtro.inicio().isAfter(filtro.fim()))
            throw ValidationException.of("inicio", "A data inicial deve ser anterior ou igual à final.");
    }
}
