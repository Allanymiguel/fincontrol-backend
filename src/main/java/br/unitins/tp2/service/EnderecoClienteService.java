package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.EnderecoClienteResponseDTO;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.Endereco;
import br.unitins.tp2.model.Municipio;

// Endereco dos CLIENTES (usuarios do sistema) com base propria de CEPs. Os contatos continuam
// com o CepService/ViaCepClient de sempre: nada daqui e usado por eles.
public interface EnderecoClienteService {

    // endereco pronto para o usuario; cepNovo = CEP manual a salvar na base junto com o usuario
    record EnderecoResolvido(Endereco endereco, CepLocal cepNovo) {
    }

    // busca do cadastro: base local -> ViaCEP (se ligado; o que achar vai para a base) -> modo manual
    EnderecoClienteResponseDTO consultar(String cep);

    // municipios da UF (selecao do modo manual), com limite de resultados
    List<Municipio> municipios(String uf, String busca, int limite);

    // bairros ou logradouros ja existentes na base local do municipio (autocompletar)
    List<String> sugestoes(Long idMunicipio, String campo, String busca, int limite);

    // Regras do back (o bloqueio na tela e so visual): CEP da base usa os dados da base; manual so
    // para CEP fora da base com a API desligada ou falha recente; municipio do IBGE e da UF do CEP.
    // Lanca ValidationException com o campo; nada e salvo aqui (o CEP manual vai em cepNovo).
    EnderecoResolvido resolver(String cep, String logradouro, String numero, String complemento, String bairro,
            Long idMunicipio);
}
