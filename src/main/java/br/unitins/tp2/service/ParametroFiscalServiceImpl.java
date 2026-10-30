package br.unitins.tp2.service;

import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.ParametroFiscalRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.ParametroFiscal;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.ParametroFiscalRepository;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ParametroFiscalServiceImpl implements ParametroFiscalService {

    @Inject
    ParametroFiscalRepository parametroFiscalRepository;

    @Inject
    AuditoriaService auditoria;

    @Override
    @Transactional
    public ParametroFiscal create(ParametroFiscalRequestDTO parametro) {
        validarDados(parametro, null);

        ParametroFiscal novoParametro = new ParametroFiscal();
        aplicarDados(novoParametro, parametro);

        parametroFiscalRepository.persist(novoParametro);
        auditoria.registrar(AcaoAuditoria.CRIACAO_PARAMETRO_FISCAL, TipoAlvoAuditoria.PARAMETRO_FISCAL, novoParametro.getId(),
                RetratoAuditoria.descricao(novoParametro), Map.of(), RetratoAuditoria.de(novoParametro));

        return novoParametro;
    }

    @Override
    @Transactional
    public void update(long id, ParametroFiscalRequestDTO parametro) {
        ParametroFiscal edicaoParametro = buscarParametroOuFalhar(id);
        validarDados(parametro, id);
        Map<String, String> antes = RetratoAuditoria.de(edicaoParametro);
        aplicarDados(edicaoParametro, parametro);
        auditoria.registrar(AcaoAuditoria.ALTERACAO_PARAMETRO_FISCAL, TipoAlvoAuditoria.PARAMETRO_FISCAL, id,
                RetratoAuditoria.descricao(edicaoParametro), antes, RetratoAuditoria.de(edicaoParametro));
    }

    @Override
    @Transactional
    public void delete(long id) {
        ParametroFiscal excluido = buscarParametroOuFalhar(id);
        Map<String, String> antes = RetratoAuditoria.de(excluido);
        parametroFiscalRepository.delete(excluido);
        auditoria.registrar(AcaoAuditoria.EXCLUSAO_PARAMETRO_FISCAL, TipoAlvoAuditoria.PARAMETRO_FISCAL, id,
                RetratoAuditoria.descricao(excluido), antes, Map.of());
    }

    @Override
    public ParametroFiscal findById(long id) {
        return buscarParametroOuFalhar(id);
    }

    @Override
    public List<ParametroFiscal> findAll(int page, int pageSize) {
        return parametroFiscalRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<ParametroFiscal> findByChave(String chave, int page, int pageSize) {
        return parametroFiscalRepository.findByChave(chave).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return parametroFiscalRepository.findAll().count();
    }

    @Override
    public long count(String chave) {
        return parametroFiscalRepository.findByChave(chave).count();
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(ParametroFiscalRequestDTO dto, Long idParametro) {
        if (dto.vigenciaFim() != null && dto.vigenciaFim().isBefore(dto.vigenciaInicio()))
            throw ValidationException.of("vigenciaFim", "O fim da vigência não pode ser anterior ao início.");

        if (parametroFiscalRepository.findByChaveAndInicioExceptId(normalizarChave(dto.chave()),
                dto.vigenciaInicio(), idParametro) != null)
            throw ValidationException.of("vigenciaInicio",
                    "Já existe um parâmetro com essa chave iniciando nessa data.");
    }

    private void aplicarDados(ParametroFiscal destino, ParametroFiscalRequestDTO origem) {
        destino.setChave(normalizarChave(origem.chave()));
        destino.setValor(origem.valor());
        destino.setVigenciaInicio(origem.vigenciaInicio());
        destino.setVigenciaFim(origem.vigenciaFim());
    }

    private String normalizarChave(String chave) {
        return chave.trim().toUpperCase();
    }

    private ParametroFiscal buscarParametroOuFalhar(long id) {
        ParametroFiscal parametro = parametroFiscalRepository.findById(id);
        if (parametro == null)
            throw new ResourceNotFoundException("Parâmetro fiscal não encontrado.");

        return parametro;
    }
}
