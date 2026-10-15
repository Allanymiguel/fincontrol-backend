package br.unitins.tp2.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import br.unitins.tp2.client.ViaCepClient;
import br.unitins.tp2.client.ViaCepResponse;
import br.unitins.tp2.dto.CepResponseDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ServicoExternoException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.mapper.MunicipioResponseMapper;
import br.unitins.tp2.model.Estado;
import br.unitins.tp2.model.Municipio;
import br.unitins.tp2.repository.EstadoRepository;
import br.unitins.tp2.repository.MunicipioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CepServiceImpl implements CepService {

    private static final Logger LOG = Logger.getLogger(CepServiceImpl.class);

    @Inject
    @RestClient
    ViaCepClient viaCepClient;

    @Inject
    EstadoRepository estadoRepository;

    @Inject
    MunicipioRepository municipioRepository;

    @Override
    @Transactional
    public CepResponseDTO buscar(String cep) {
        String cepLimpo = cep == null ? "" : cep.replaceAll("\\D", "");
        if (!cepLimpo.matches("\\d{8}"))
            throw ValidationException.of("cep", "O CEP deve conter 8 dígitos.");

        ViaCepResponse resposta = consultarViaCep(cepLimpo);

        if (resposta == null || resposta.naoEncontrado())
            throw new ResourceNotFoundException("CEP não encontrado.");

        Municipio municipio = resolverMunicipio(resposta);

        return new CepResponseDTO(
                cepLimpo,
                resposta.logradouro(),
                resposta.complemento(),
                resposta.bairro(),
                MunicipioResponseMapper.toResponse(municipio));
    }

    private ViaCepResponse consultarViaCep(String cep) {
        try {
            return viaCepClient.buscar(cep);
        } catch (RuntimeException e) {
            // timeout, sem internet, ViaCEP fora do ar, 400 do ViaCEP...
            LOG.warnf(e, "Falha ao consultar o ViaCEP para o CEP %s", cep);
            throw new ServicoExternoException(
                    "Não foi possível consultar o CEP agora. Tente novamente ou preencha o endereço mais tarde.", e);
        }
    }

    // O ViaCEP devolve texto (cidade, UF) + codigo IBGE. Aqui transformamos isso em dado
    // estruturado: o Estado e encontrado pela sigla e o Municipio pelo codigo IBGE.
    // Se o municipio ainda nao existe no banco, ele e cadastrado na hora.
    private Municipio resolverMunicipio(ViaCepResponse resposta) {
        Municipio municipio = municipioRepository.findByCodigoIbge(resposta.ibge());
        if (municipio != null)
            return municipio;

        municipio = new Municipio();
        municipio.setNome(resposta.localidade());
        municipio.setCodigoIbge(resposta.ibge());
        municipio.setEstado(resolverEstado(resposta));
        municipioRepository.persist(municipio);

        return municipio;
    }

    private Estado resolverEstado(ViaCepResponse resposta) {
        Estado estado = estadoRepository.findBySigla(resposta.uf());
        if (estado != null)
            return estado;

        estado = new Estado();
        estado.setSigla(resposta.uf().toUpperCase());
        estado.setNome(resposta.estado() != null ? resposta.estado() : resposta.uf().toUpperCase());
        estadoRepository.persist(estado);

        return estado;
    }
}
