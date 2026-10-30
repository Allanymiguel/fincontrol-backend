package br.unitins.tp2.dto;

import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.SituacaoCep;

// Resultado da busca de CEP do cadastro de cliente.
// encontrado: veio da base local ou do ViaCEP (e ja foi salvo na base); os campos bloqueados vem
// preenchidos e o back ignora o que o cliente mandar neles. Num CEP geral de cidade, logradouro e
// bairro vem vazios e desbloqueados.
// modoManual: CEP nao encontrado (ou consulta externa desligada/falhou): o cliente escolhe o
// municipio da UF sugerida pela faixa do CEP e digita bairro e logradouro.
public record EnderecoClienteResponseDTO(
    String cep,
    boolean encontrado,
    FonteConsultaCep fonte,
    OrigemCep origem,
    SituacaoCep situacao,
    String logradouro,
    String bairro,
    MunicipioResponseDTO municipio,
    boolean logradouroBloqueado,
    boolean bairroBloqueado,
    boolean modoManual,
    // sigla da UF pela faixa do CEP (sempre presente)
    String uf,
    String mensagem
) {
}
