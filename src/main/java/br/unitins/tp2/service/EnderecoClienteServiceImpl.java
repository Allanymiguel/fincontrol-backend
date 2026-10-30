package br.unitins.tp2.service;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.unitins.tp2.client.ViaCepEnderecoClient;
import br.unitins.tp2.client.ViaCepResponse;
import br.unitins.tp2.dto.EnderecoClienteResponseDTO;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.MunicipioResponseMapper;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.ConsultaCep;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import br.unitins.tp2.model.SituacaoCep;
import br.unitins.tp2.repository.CepLocalRepository;
import br.unitins.tp2.repository.ConsultaCepRepository;
import br.unitins.tp2.repository.FaixaCepUfRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import br.unitins.tp2.util.RegrasCep;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EnderecoClienteServiceImpl implements EnderecoClienteService {

    private static final Logger LOG = Logger.getLogger(EnderecoClienteServiceImpl.class);

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

    // resultado do ViaCEP: o CEP ja salvo na base (achou) ou nulo, e se a consulta falhou
    private record Externo(CepLocal cep, boolean falhou) {
    }

    @Override
    public EnderecoClienteResponseDTO consultar(String cepInformado) {
        String cep = cepOuFalhar(cepInformado);
        String uf = ufOuFalhar(cep);

        CepLocal local = cepLocalRepository.findByCep(cep);
        registrarConsulta(cep, FonteConsultaCep.BASE_LOCAL,
                local != null ? ResultadoConsultaCep.ENCONTRADO : ResultadoConsultaCep.NAO_ENCONTRADO);
        if (local != null)
            return encontrado(local, FonteConsultaCep.BASE_LOCAL, uf);

        if (!configuracao.isApiExternaHabilitada())
            return manual(cep, uf, "A consulta externa de CEP está desligada. Preencha o endereço manualmente.");

        Externo externo = consultarViaCep(cep);
        if (externo.cep() != null)
            return encontrado(externo.cep(), FonteConsultaCep.VIACEP, uf);
        return manual(cep, uf, externo.falhou()
                ? "Não foi possível consultar o CEP agora. Preencha o endereço manualmente."
                : "CEP não encontrado. Confira o número ou preencha o endereço manualmente.");
    }

    @Override
    public List<Municipio> municipios(String uf, String busca, int limite) {
        if (uf == null || !uf.trim().matches("[A-Za-z]{2}"))
            throw ValidationException.of("uf", "Informe a UF com 2 letras.");
        return municipioRepository.findByUf(uf, busca, limite);
    }

    @Override
    public List<String> sugestoes(Long idMunicipio, String campo, String busca, int limite) {
        if (idMunicipio == null)
            throw ValidationException.of("idMunicipio", "Informe o município.");
        if (!"bairro".equals(campo) && !"logradouro".equals(campo))
            throw ValidationException.of("campo", "O campo deve ser \"bairro\" ou \"logradouro\".");
        return cepLocalRepository.findSugestoes(idMunicipio, campo, busca, limite);
    }

    @Override
    public EnderecoResolvido resolver(String cepInformado, String logradouro, String numero, String complemento,
            String bairro, Long idMunicipio) {
        String cep = cepOuFalhar(cepInformado);
        String uf = ufOuFalhar(cep);
        LocalDateTime janela = RegrasCep.inicioDaJanela(LocalDateTime.now());

        // fora da base e sem falha recente: o proprio back consulta o ViaCEP (o front nao decide)
        CepLocal local = cepLocalRepository.findByCep(cep);
        if (local == null && configuracao.isApiExternaHabilitada()
                && !consultaRepository.existeFalhaOuNaoEncontradoDesde(cep, janela))
            local = consultarViaCep(cep).cep();

        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setNumero(numero == null ? null : numero.trim());
        endereco.setComplemento(RegrasCep.vazioParaNulo(complemento));

        // CEP da base: valem os dados da base; o cliente so completa o que estiver vazio nela
        if (local != null) {
            endereco.setLogradouro(local.getLogradouro() != null ? local.getLogradouro()
                    : obrigatorio(logradouro, "logradouro", "Informe o logradouro."));
            endereco.setBairro(local.getBairro() != null ? local.getBairro()
                    : obrigatorio(bairro, "bairro", "Informe o bairro."));
            endereco.setMunicipio(local.getMunicipio());
            return new EnderecoResolvido(endereco, null);
        }

        boolean falhaRecente = consultaRepository.existeFalhaOuNaoEncontradoDesde(cep, janela);
        if (!RegrasCep.aceitaManual(configuracao.isApiExternaHabilitada(), falhaRecente))
            throw ValidationException.of("cep", "Consulte o CEP antes de preencher o endereço manualmente.");

        Municipio municipio = idMunicipio == null ? null : municipioRepository.findById(idMunicipio);
        if (municipio == null)
            throw ValidationException.of("idMunicipio", "Selecione o município na lista.");
        if (!RegrasCep.municipioCompativel(municipio, uf))
            throw ValidationException.of("idMunicipio",
                    "O município escolhido não é de " + uf + ", a UF deste CEP.");
        endereco.setLogradouro(obrigatorio(logradouro, "logradouro", "Informe o logradouro."));
        endereco.setBairro(obrigatorio(bairro, "bairro", "Informe o bairro."));
        endereco.setMunicipio(municipio);

        // o proximo cadastro com este CEP ja vem preenchido (manual e nao verificado ate o admin conferir)
        CepLocal novo = new CepLocal();
        novo.setCep(cep);
        novo.setLogradouro(endereco.getLogradouro());
        novo.setBairro(endereco.getBairro());
        novo.setMunicipio(municipio);
        novo.setOrigem(OrigemCep.MANUAL);
        novo.setSituacao(SituacaoCep.NAO_VERIFICADO);
        return new EnderecoResolvido(endereco, novo);
    }

    // ===== ViaCEP =====

    // Consulta o ViaCEP e registra o resultado em transacao propria (vale mesmo se o cadastro falhar
    // depois). Achou: o CEP entra na base como API/verificado; logradouro e bairro vazios ficam nulos.
    private Externo consultarViaCep(String cep) {
        ViaCepResponse resposta;
        try {
            resposta = viaCepClient.buscar(cep);
        } catch (RuntimeException e) {
            LOG.warnf("ViaCEP indisponivel para o CEP %s: %s", cep, e.getMessage());
            QuarkusTransaction.requiringNew().run(() -> registrarConsulta(cep, FonteConsultaCep.VIACEP,
                    ResultadoConsultaCep.FALHA));
            return new Externo(null, true);
        }

        Municipio municipio = resposta == null || resposta.naoEncontrado() ? null
                : municipioRepository.findByCodigoIbge(resposta.ibge());
        if (municipio == null) {
            QuarkusTransaction.requiringNew().run(() -> registrarConsulta(cep, FonteConsultaCep.VIACEP,
                    ResultadoConsultaCep.NAO_ENCONTRADO));
            return new Externo(null, false);
        }

        CepLocal salvo = QuarkusTransaction.requiringNew().call(() -> {
            registrarConsulta(cep, FonteConsultaCep.VIACEP, ResultadoConsultaCep.ENCONTRADO);
            CepLocal existente = cepLocalRepository.findByCep(cep);
            if (existente != null)
                return existente;
            CepLocal novo = new CepLocal();
            novo.setCep(cep);
            novo.setLogradouro(RegrasCep.vazioParaNulo(resposta.logradouro()));
            novo.setBairro(RegrasCep.vazioParaNulo(resposta.bairro()));
            novo.setMunicipio(municipio);
            novo.setOrigem(OrigemCep.API);
            novo.setSituacao(SituacaoCep.VERIFICADO);
            cepLocalRepository.persist(novo);
            return novo;
        });
        return new Externo(salvo, false);
    }

    private void registrarConsulta(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado) {
        QuarkusTransaction.joiningExisting().run(() -> {
            ConsultaCep consulta = new ConsultaCep();
            consulta.setCep(cep);
            consulta.setDataHora(LocalDateTime.now());
            consulta.setFonte(fonte);
            consulta.setResultado(resultado);
            consultaRepository.persist(consulta);
        });
    }

    // ===== respostas =====

    private EnderecoClienteResponseDTO encontrado(CepLocal cep, FonteConsultaCep fonte, String uf) {
        boolean logradouro = cep.getLogradouro() != null;
        boolean bairro = cep.getBairro() != null;
        String mensagem = logradouro && bairro ? null
                : "CEP geral da cidade: complete o que estiver em branco.";
        return new EnderecoClienteResponseDTO(cep.getCep(), true, fonte, cep.getOrigem(), cep.getSituacao(),
                cep.getLogradouro(), cep.getBairro(), MunicipioResponseMapper.toResponse(cep.getMunicipio()),
                logradouro, bairro, false, uf, mensagem);
    }

    private EnderecoClienteResponseDTO manual(String cep, String uf, String mensagem) {
        return new EnderecoClienteResponseDTO(cep, false, null, null, null, null, null, null, false, false, true, uf,
                mensagem);
    }

    // ===== validacoes =====

    private String cepOuFalhar(String cep) {
        String normalizado = RegrasCep.normalizar(cep);
        if (normalizado == null)
            throw ValidationException.of("cep", "O CEP deve conter 8 dígitos.");
        return normalizado;
    }

    private String ufOuFalhar(String cep) {
        var faixa = faixaRepository.findByCep(cep);
        if (faixa == null)
            throw ValidationException.of("cep", "CEP inválido: não pertence a nenhuma UF.");
        return faixa.getEstado().getSigla();
    }

    private static String obrigatorio(String valor, String campo, String mensagem) {
        String texto = RegrasCep.vazioParaNulo(valor);
        if (texto == null)
            throw ValidationException.of(campo, mensagem);
        return texto;
    }
}
