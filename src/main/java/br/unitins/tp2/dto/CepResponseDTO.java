package br.unitins.tp2.dto;

// Resposta da consulta de CEP ja com o municipio resolvido no nosso banco (id pronto para o cadastro)
public record CepResponseDTO(
    String cep,
    String logradouro,
    String complemento,
    String bairro,
    MunicipioResponseDTO municipio
) {
}
