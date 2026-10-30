package br.unitins.tp2.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

// Correcao de um CEP da base pelo admin (revisao). Logradouro e bairro podem ficar vazios num
// CEP geral de cidade. verificar = marcar como verificado ao salvar.
public record CepLocalRequestDTO(
    @Length(max = 120, message = "O logradouro deve conter no máximo 120 caracteres.")
    String logradouro,

    @Length(max = 60, message = "O bairro deve conter no máximo 60 caracteres.")
    String bairro,

    @NotNull(message = "Selecione o município.")
    Long idMunicipio,

    boolean verificar
) {
}
