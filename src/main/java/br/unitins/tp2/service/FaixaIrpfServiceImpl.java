package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.FaixaIrpfRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.FaixaIrpf;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.FaixaIrpfRepository;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class FaixaIrpfServiceImpl implements FaixaIrpfService {

    @Inject
    FaixaIrpfRepository faixaIrpfRepository;

    @Inject
    AuditoriaService auditoria;

    @Override
    @Transactional
    public FaixaIrpf create(FaixaIrpfRequestDTO faixa) {
        validarDados(faixa, null);

        FaixaIrpf novaFaixa = new FaixaIrpf();
        aplicarDados(novaFaixa, faixa);

        faixaIrpfRepository.persist(novaFaixa);
        auditoria.registrar(AcaoAuditoria.CRIACAO_FAIXA_IRPF, TipoAlvoAuditoria.FAIXA_IRPF, novaFaixa.getId(),
                RetratoAuditoria.descricao(novaFaixa), Map.of(), RetratoAuditoria.de(novaFaixa));

        return novaFaixa;
    }

    @Override
    @Transactional
    public void update(long id, FaixaIrpfRequestDTO faixa) {
        FaixaIrpf edicaoFaixa = buscarFaixaOuFalhar(id);
        validarDados(faixa, id);
        Map<String, String> antes = RetratoAuditoria.de(edicaoFaixa);
        aplicarDados(edicaoFaixa, faixa);
        auditoria.registrar(AcaoAuditoria.ALTERACAO_FAIXA_IRPF, TipoAlvoAuditoria.FAIXA_IRPF, id,
                RetratoAuditoria.descricao(edicaoFaixa), antes, RetratoAuditoria.de(edicaoFaixa));
    }

    @Override
    @Transactional
    public void delete(long id) {
        FaixaIrpf excluida = buscarFaixaOuFalhar(id);
        Map<String, String> antes = RetratoAuditoria.de(excluida);
        faixaIrpfRepository.delete(excluida);
        auditoria.registrar(AcaoAuditoria.EXCLUSAO_FAIXA_IRPF, TipoAlvoAuditoria.FAIXA_IRPF, id,
                RetratoAuditoria.descricao(excluida), antes, Map.of());
    }

    @Override
    public FaixaIrpf findById(long id) {
        return buscarFaixaOuFalhar(id);
    }

    @Override
    public List<FaixaIrpf> findAll(int page, int pageSize) {
        return faixaIrpfRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<FaixaIrpf> findByVigencia(LocalDate vigenciaInicio, int page, int pageSize) {
        return faixaIrpfRepository.findByVigencia(vigenciaInicio).page(page, pageSize).list();
    }

    @Override
    public List<LocalDate> findVigencias() {
        return faixaIrpfRepository.findVigencias();
    }

    @Override
    public long count() {
        return faixaIrpfRepository.findAll().count();
    }

    @Override
    public long count(LocalDate vigenciaInicio) {
        return faixaIrpfRepository.findByVigencia(vigenciaInicio).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(FaixaIrpfRequestDTO dto, Long idFaixa) {
        if (dto.limiteSuperior() != null && dto.limiteSuperior().compareTo(dto.limiteInferior()) <= 0)
            throw ValidationException.of("limiteSuperior", "O limite superior deve ser maior que o inferior.");

        if (faixaIrpfRepository.findByVigenciaAndLimiteInferiorExceptId(dto.vigenciaInicio(),
                dto.limiteInferior(), idFaixa) != null)
            throw ValidationException.of("limiteInferior",
                    "Já existe uma faixa nessa vigência começando nesse limite inferior.");
    }

    private void aplicarDados(FaixaIrpf destino, FaixaIrpfRequestDTO origem) {
        destino.setLimiteInferior(origem.limiteInferior());
        destino.setLimiteSuperior(origem.limiteSuperior());
        destino.setAliquota(origem.aliquota());
        destino.setParcelaDeduzir(origem.parcelaDeduzir());
        destino.setVigenciaInicio(origem.vigenciaInicio());
    }

    private FaixaIrpf buscarFaixaOuFalhar(long id) {
        FaixaIrpf faixa = faixaIrpfRepository.findById(id);
        if (faixa == null)
            throw new ResourceNotFoundException("Faixa do IRPF não encontrada.");

        return faixa;
    }
}
