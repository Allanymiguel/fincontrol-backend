package br.unitins.tp2.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.unitins.tp2.client.ViaCepEnderecoClient;
import br.unitins.tp2.client.ViaCepResponse;
import br.unitins.tp2.dto.CepLocalRequestDTO;
import br.unitins.tp2.dto.ResultadosCepDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.ConsultaCep;
import br.unitins.tp2.model.FaixaCepUf;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import br.unitins.tp2.model.SituacaoCep;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.CepLocalRepository;
import br.unitins.tp2.repository.ConsultaCepRepository;
import br.unitins.tp2.repository.FaixaCepUfRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import br.unitins.tp2.util.RegrasCep;
import br.unitins.tp2.util.RetratoAuditoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CepAdminServiceImpl implements CepAdminService {

    private static final Logger LOG = Logger.getLogger(CepAdminServiceImpl.class);

    @Inject
    @RestClient
    ViaCepEnderecoClient viaCepClient;

    @Inject
    CepLocalRepository cepLocalRepository;

    @Inject
    ConsultaCepRepository consultaRepository;

    @Inject
    FaixaCepUfRepository faixaRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Inject
    ConfiguracaoCepService configuracao;

    @Inject
    AuditoriaService auditoria;

    // o que a revalidacao concluiu sobre um CEP
    private enum Conferencia {
        VERIFICADO, DIVERGENTE, FALHA
    }

    @Override
    public List<CepLocal> buscar(String cep, OrigemCep origem, SituacaoCep situacao, String uf, int page,
            int pageSize) {
        return cepLocalRepository.findByFiltros(cep, origem, situacao, uf).page(page, pageSize).list();
    }

    @Override
    public long contar(String cep, OrigemCep origem, SituacaoCep situacao, String uf) {
        return cepLocalRepository.findByFiltros(cep, origem, situacao, uf).count();
    }

    @Override
    public CepLocal findById(long id) {
        CepLocal cep = cepLocalRepository.findById(id);
        if (cep == null)
            throw new ResourceNotFoundException("CEP não encontrado na base.");
        return cep;
    }

    // revisao do admin: corrige logradouro, bairro e municipio (da UF do CEP) e, se pedir, verifica
    @Override
    @Transactional
    public CepLocal corrigir(long id, CepLocalRequestDTO dto) {
        CepLocal cep = findById(id);
        Municipio municipio = municipioRepository.findById(dto.idMunicipio());
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Município não encontrado.");
        String uf = ufDoCep(cep.getCep());
        if (!RegrasCep.municipioCompativel(municipio, uf))
            throw ValidationException.of("idMunicipio", "O município escolhido não é de " + uf + ", a UF deste CEP.");

        Map<String, String> antes = RetratoAuditoria.de(cep);
        cep.setLogradouro(RegrasCep.vazioParaNulo(dto.logradouro()));
        cep.setBairro(RegrasCep.vazioParaNulo(dto.bairro()));
        cep.setMunicipio(municipio);
        if (dto.verificar())
            marcarVerificado(cep);
        auditoria.registrar(AcaoAuditoria.CORRECAO_CEP, TipoAlvoAuditoria.CEP, cep.getId(), cep.getCep(), antes,
                RetratoAuditoria.de(cep));
        return cep;
    }

    @Override
    @Transactional
    public CepLocal verificar(long id) {
        CepLocal cep = findById(id);
        Map<String, String> antes = RetratoAuditoria.de(cep);
        marcarVerificado(cep);
        auditoria.registrar(AcaoAuditoria.VERIFICACAO_CEP, TipoAlvoAuditoria.CEP, cep.getId(), cep.getCep(), antes,
                RetratoAuditoria.de(cep));
        return cep;
    }

    @Override
    @Transactional
    public CepLocal revalidar(long id) {
        exigirApiLigada();
        CepLocal cep = findById(id);
        Map<String, String> antes = RetratoAuditoria.de(cep);
        if (conferir(cep) == Conferencia.FALHA)
            throw new ServicoExternoException("Não foi possível consultar o ViaCEP agora. Tente novamente.", null);
        auditoria.registrar(AcaoAuditoria.REVALIDACAO_CEP, TipoAlvoAuditoria.CEP, cep.getId(), cep.getCep(), antes,
                RetratoAuditoria.de(cep));
        return cep;
    }

    // um evento de auditoria para o lote, com o resumo
    @Override
    @Transactional
    public ResultadosCepDTO.RevalidacaoLote revalidarManuais() {
        exigirApiLigada();
        int verificados = 0, divergentes = 0, falhas = 0;
        List<CepLocal> manuais = cepLocalRepository.findManuaisNaoVerificados();
        for (CepLocal cep : manuais) {
            switch (conferir(cep)) {
                case VERIFICADO -> verificados++;
                case DIVERGENTE -> divergentes++;
                case FALHA -> falhas++;
            }
        }
        Map<String, String> resumo = new LinkedHashMap<>();
        resumo.put("CEPs manuais conferidos", String.valueOf(manuais.size()));
        resumo.put("Verificados", String.valueOf(verificados));
        resumo.put("Divergentes", String.valueOf(divergentes));
        resumo.put("Falhas na consulta", String.valueOf(falhas));
        auditoria.registrar(AcaoAuditoria.REVALIDACAO_CEP, TipoAlvoAuditoria.CEP, null,
                "Revalidação em lote dos CEPs manuais", Map.of(), resumo);
        return new ResultadosCepDTO.RevalidacaoLote(manuais.size(), verificados, divergentes, falhas);
    }

    // Formato: cabecalho "cep;logradouro;bairro;codigo_ibge", UTF-8, ate 5.000 linhas (1 MB no resource).
    // CEP verificado nao e sobrescrito; os demais entram (ou sao atualizados) como IMPORTACAO e nao verificados.
    @Override
    @Transactional
    public ResultadosCepDTO.Importacao importar(String conteudo) {
        String[] linhas = conteudo == null ? new String[0] : conteudo.split("\\r?\\n");
        if (linhas.length == 0 || !RegrasCep.cabecalhoValido(linhas[0]))
            throw ValidationException.of("arquivo",
                    "A primeira linha deve ser o cabeçalho \"" + RegrasCep.CABECALHO_CSV + "\".");
        if (linhas.length - 1 > RegrasCep.MAX_LINHAS_CSV)
            throw ValidationException.of("arquivo",
                    "O arquivo pode ter no máximo " + RegrasCep.MAX_LINHAS_CSV + " linhas de dados.");

        int importados = 0, atualizados = 0;
        List<ResultadosCepDTO.Importacao.Rejeicao> rejeitados = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (int i = 1; i < linhas.length; i++) {
            if (linhas[i].isBlank())
                continue;
            int numeroLinha = i + 1;
            RegrasCep.LinhaCsv linha;
            try {
                linha = RegrasCep.lerLinha(linhas[i]);
            } catch (IllegalArgumentException e) {
                rejeitados.add(new ResultadosCepDTO.Importacao.Rejeicao(numeroLinha, null, e.getMessage()));
                continue;
            }
            String motivo = motivoParaRejeitar(linha, vistos);
            if (motivo != null) {
                rejeitados.add(new ResultadosCepDTO.Importacao.Rejeicao(numeroLinha, linha.cep(), motivo));
                continue;
            }
            CepLocal cep = cepLocalRepository.findByCep(linha.cep());
            boolean novo = cep == null;
            if (novo) {
                cep = new CepLocal();
                cep.setCep(linha.cep());
            }
            cep.setLogradouro(linha.logradouro());
            cep.setBairro(linha.bairro());
            cep.setMunicipio(municipioRepository.findByCodigoIbge(linha.codigoIbge()));
            cep.setOrigem(OrigemCep.IMPORTACAO);
            cep.setSituacao(SituacaoCep.NAO_VERIFICADO);
            limparDivergencia(cep);
            if (novo) {
                cepLocalRepository.persist(cep);
                importados++;
            } else {
                atualizados++;
            }
        }

        Map<String, String> resumo = new LinkedHashMap<>();
        resumo.put("Importados", String.valueOf(importados));
        resumo.put("Atualizados", String.valueOf(atualizados));
        resumo.put("Rejeitados", String.valueOf(rejeitados.size()));
        auditoria.registrar(AcaoAuditoria.IMPORTACAO_CEP, TipoAlvoAuditoria.CEP, null, "Importação de CEPs por CSV",
                Map.of(), resumo);
        return new ResultadosCepDTO.Importacao(importados, atualizados, rejeitados);
    }

    private String motivoParaRejeitar(RegrasCep.LinhaCsv linha, Set<String> vistos) {
        if (!vistos.add(linha.cep()))
            return "CEP repetido no arquivo.";
        String uf = faixaUf(linha.cep());
        if (uf == null)
            return "CEP não pertence a nenhuma UF.";
        Municipio municipio = municipioRepository.findByCodigoIbge(linha.codigoIbge());
        if (municipio == null)
            return "Município não encontrado pelo código IBGE.";
        if (!RegrasCep.municipioCompativel(municipio, uf))
            return "O município não é de " + uf + ", a UF deste CEP.";
        CepLocal existente = cepLocalRepository.findByCep(linha.cep());
        if (existente != null && existente.getSituacao() == SituacaoCep.VERIFICADO)
            return "CEP já verificado na base: não foi sobrescrito.";
        return null;
    }

    @Override
    public ResultadosCepDTO.Configuracao configuracao() {
        return new ResultadosCepDTO.Configuracao(configuracao.isApiExternaHabilitada());
    }

    @Override
    public ResultadosCepDTO.Configuracao definirApiExterna(boolean habilitada) {
        boolean anterior = configuracao.isApiExternaHabilitada();
        configuracao.definirApiExternaHabilitada(habilitada);
        auditoria.registrar(AcaoAuditoria.ALTERACAO_API_CEP, TipoAlvoAuditoria.CONFIGURACAO, null,
                "Consulta externa de CEP (ViaCEP)", Map.of("Consulta externa", anterior ? "Ligada" : "Desligada"),
                Map.of("Consulta externa", habilitada ? "Ligada" : "Desligada"));
        return configuracao();
    }

    @Override
    public List<ConsultaCep> consultas(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado,
            LocalDate inicio, LocalDate fim, int page, int pageSize) {
        return consultaRepository.findByFiltros(cep, fonte, resultado, inicioDe(inicio, fim), fimDe(fim))
                .page(page, pageSize).list();
    }

    @Override
    public long contarConsultas(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado,
            LocalDate inicio, LocalDate fim) {
        return consultaRepository.findByFiltros(cep, fonte, resultado, inicioDe(inicio, fim), fimDe(fim)).count();
    }

    // ===== revalidacao =====

    // Pergunta ao ViaCEP e marca o CEP: tudo confere = verificado; difere ou o ViaCEP nao conhece =
    // divergente (guarda o que ele devolveu, para a tela mostrar a diferenca). Falha nao muda o CEP.
    private Conferencia conferir(CepLocal cep) {
        ViaCepResponse resposta;
        try {
            resposta = viaCepClient.buscar(cep.getCep());
        } catch (RuntimeException e) {
            LOG.warnf("ViaCEP indisponivel na revalidacao do CEP %s: %s", cep.getCep(), e.getMessage());
            registrarConsulta(cep.getCep(), ResultadoConsultaCep.FALHA);
            return Conferencia.FALHA;
        }
        cep.setDataRevalidacao(LocalDateTime.now());
        if (resposta == null || resposta.naoEncontrado()) {
            registrarConsulta(cep.getCep(), ResultadoConsultaCep.NAO_ENCONTRADO);
            cep.setSituacao(SituacaoCep.DIVERGENTE);
            cep.setLogradouroExterno(null);
            cep.setBairroExterno(null);
            cep.setMunicipioExterno("CEP não encontrado no ViaCEP");
            return Conferencia.DIVERGENTE;
        }
        registrarConsulta(cep.getCep(), ResultadoConsultaCep.ENCONTRADO);
        boolean confere = RegrasCep.confere(cep.getLogradouro(), resposta.logradouro())
                && RegrasCep.confere(cep.getBairro(), resposta.bairro())
                && cep.getMunicipio().getCodigoIbge().equals(resposta.ibge());
        if (confere) {
            marcarVerificado(cep);
            return Conferencia.VERIFICADO;
        }
        cep.setSituacao(SituacaoCep.DIVERGENTE);
        cep.setLogradouroExterno(RegrasCep.vazioParaNulo(resposta.logradouro()));
        cep.setBairroExterno(RegrasCep.vazioParaNulo(resposta.bairro()));
        cep.setMunicipioExterno(resposta.localidade() + " - " + resposta.uf());
        return Conferencia.DIVERGENTE;
    }

    private void registrarConsulta(String cep, ResultadoConsultaCep resultado) {
        ConsultaCep consulta = new ConsultaCep();
        consulta.setCep(cep);
        consulta.setDataHora(LocalDateTime.now());
        consulta.setFonte(FonteConsultaCep.VIACEP);
        consulta.setResultado(resultado);
        consultaRepository.persist(consulta);
    }

    private void marcarVerificado(CepLocal cep) {
        cep.setSituacao(SituacaoCep.VERIFICADO);
        limparDivergencia(cep);
    }

    private static void limparDivergencia(CepLocal cep) {
        cep.setLogradouroExterno(null);
        cep.setBairroExterno(null);
        cep.setMunicipioExterno(null);
    }

    private void exigirApiLigada() {
        if (!configuracao.isApiExternaHabilitada())
            throw ValidationException.of("apiExternaHabilitada",
                    "A consulta externa está desligada. Ligue-a para revalidar com o ViaCEP.");
    }

    private String ufDoCep(String cep) {
        String uf = faixaUf(cep);
        if (uf == null)
            throw ValidationException.of("cep", "CEP inválido: não pertence a nenhuma UF.");
        return uf;
    }

    private String faixaUf(String cep) {
        FaixaCepUf faixa = faixaRepository.findByCep(cep);
        return faixa == null ? null : faixa.getEstado().getSigla();
    }

    private static LocalDateTime inicioDe(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && inicio.isAfter(fim))
            throw ValidationException.of("inicio", "A data inicial deve ser anterior ou igual à final.");
        return inicio == null ? null : inicio.atStartOfDay();
    }

    private static LocalDateTime fimDe(LocalDate fim) {
        return fim == null ? null : fim.plusDays(1).atStartOfDay();
    }
}
