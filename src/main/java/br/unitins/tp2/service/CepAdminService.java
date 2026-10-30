package br.unitins.tp2.service;

import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.CepLocalRequestDTO;
import br.unitins.tp2.dto.ResultadosCepDTO;
import br.unitins.tp2.model.CepLocal;
import br.unitins.tp2.model.ConsultaCep;
import br.unitins.tp2.model.FonteConsultaCep;
import br.unitins.tp2.model.OrigemCep;
import br.unitins.tp2.model.ResultadoConsultaCep;
import br.unitins.tp2.model.SituacaoCep;
import jakarta.validation.Valid;

// Gestao da base de CEPs de clientes pelo admin: revisao, correcao, verificacao, revalidacao com o
// ViaCEP, importacao CSV, interruptor da consulta externa e log de consultas. Tudo auditado.
public interface CepAdminService {

    List<CepLocal> buscar(String cep, OrigemCep origem, SituacaoCep situacao, String uf, int page, int pageSize);

    long contar(String cep, OrigemCep origem, SituacaoCep situacao, String uf);

    CepLocal findById(long id);

    CepLocal corrigir(long id, @Valid CepLocalRequestDTO dto);

    CepLocal verificar(long id);

    // confere com o ViaCEP: bateu = verificado; nao bateu ou o ViaCEP nao conhece = divergente
    CepLocal revalidar(long id);

    // revalida todos os manuais nao verificados
    ResultadosCepDTO.RevalidacaoLote revalidarManuais();

    ResultadosCepDTO.Importacao importar(String conteudo);

    ResultadosCepDTO.Configuracao configuracao();

    ResultadosCepDTO.Configuracao definirApiExterna(boolean habilitada);

    List<ConsultaCep> consultas(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado, LocalDate inicio,
            LocalDate fim, int page, int pageSize);

    long contarConsultas(String cep, FonteConsultaCep fonte, ResultadoConsultaCep resultado, LocalDate inicio,
            LocalDate fim);
}
