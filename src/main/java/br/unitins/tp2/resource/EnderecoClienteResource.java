package br.unitins.tp2.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import br.unitins.tp2.dto.EnderecoClienteResponseDTO;
import br.unitins.tp2.dto.MunicipioResponseDTO;
import br.unitins.tp2.mapper.MunicipioResponseMapper;
import br.unitins.tp2.service.EnderecoClienteService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// Endereco do cadastro de CLIENTES (auto-registro e formulario completo do admin). Publico porque o
// auto-registro acontece antes do login; so consultas, com limite de resultados. A gestao da base
// e so do admin (CepAdminResource). Os contatos continuam usando GET /cep/{cep}.
@PermitAll
@Path("enderecos-cliente")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Endereço de cliente")
public class EnderecoClienteResource {

    private static final int MAX_MUNICIPIOS = 50;
    private static final int MAX_SUGESTOES = 20;

    @Inject
    EnderecoClienteService service;

    @GET
    @Path("/cep/{cep}")
    @Operation(summary = "Busca o CEP: base local, depois ViaCEP (se ligado) e, sem resultado, modo manual",
            description = "Encontrado: dados para preencher e quais campos bloquear. Modo manual: a UF da faixa do "
                    + "CEP para o cliente escolher o município e digitar bairro e logradouro.")
    public EnderecoClienteResponseDTO consultar(@PathParam("cep") String cep) {
        return service.consultar(cep);
    }

    @GET
    @Path("/municipios")
    @Operation(summary = "Municípios da UF (lista oficial do IBGE), filtrados pelo nome")
    public List<MunicipioResponseDTO> municipios(@QueryParam("uf") String uf, @QueryParam("busca") String busca,
            @QueryParam("limite") @DefaultValue("20") int limite) {
        return service.municipios(uf, busca, limitar(limite, MAX_MUNICIPIOS)).stream()
                .map(MunicipioResponseMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/sugestoes")
    @Operation(summary = "Bairros ou logradouros já existentes na base de CEPs do município (autocompletar)")
    public List<String> sugestoes(@QueryParam("idMunicipio") Long idMunicipio, @QueryParam("campo") String campo,
            @QueryParam("busca") String busca, @QueryParam("limite") @DefaultValue("10") int limite) {
        return service.sugestoes(idMunicipio, campo, busca, limitar(limite, MAX_SUGESTOES));
    }

    private static int limitar(int limite, int maximo) {
        return Math.max(1, Math.min(limite, maximo));
    }
}
