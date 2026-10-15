package br.unitins.tp2.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Formato devolvido por https://viacep.com.br/ws/{cep}/json
// Quando o CEP tem formato valido mas nao existe, o ViaCEP responde 200 com {"erro": "true"}
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    String localidade,
    String uf,
    String estado,
    String ibge,
    String erro
) {
    public boolean naoEncontrado() {
        return "true".equalsIgnoreCase(erro);
    }
}
