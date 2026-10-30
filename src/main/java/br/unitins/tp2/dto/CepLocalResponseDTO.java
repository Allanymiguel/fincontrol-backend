package br.unitins.tp2.dto;

import java.time.LocalDateTime;

import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.SituacaoCep;

public record CepLocalResponseDTO(
    Long id,
    String cep,
    String logradouro,
    String bairro,
    MunicipioResponseDTO municipio,
    OrigemCep origem,
    SituacaoCep situacao,
    LocalDateTime dataCadastro,
    LocalDateTime dataAlteracao,
    LocalDateTime dataRevalidacao,
    // o que o ViaCEP devolveu na ultima revalidacao divergente; null se nao ha divergencia
    Divergencia divergencia
) {

    public record Divergencia(String logradouro, String bairro, String municipio) {
    }
}
