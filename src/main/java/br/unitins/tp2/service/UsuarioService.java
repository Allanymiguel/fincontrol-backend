package br.unitins.tp2.service;

import java.util.List;

import br.unitins.tp2.dto.CadastroCompletoDTO;
import br.unitins.tp2.dto.CadastroSimplesDTO;
import br.unitins.tp2.dto.EventoLinhaDoTempoDTO;
import br.unitins.tp2.dto.UsuarioEdicaoCompletaDTO;
import br.unitins.tp2.dto.UsuarioEdicaoSimplesDTO;
import br.unitins.tp2.dto.UsuarioResumoResponseDTO;
import br.unitins.tp2.model.Papel;
import br.unitins.tp2.model.Usuario;
import jakarta.validation.Valid;

// Cadastro da conta (Keycloak + perfil local) e gestao de usuarios pelo admin
public interface UsuarioService {

    // duas formas de cadastro: simples (so o acesso) e completo (com dados fiscais e endereco).
    // O cadastro publico usa papel USER; o admin escolhe USER ou ADMIN.
    Usuario cadastrarSimples(@Valid CadastroSimplesDTO cadastro, Papel papel);
    Usuario cadastrarCompleto(@Valid CadastroCompletoDTO cadastro, Papel papel);
    // edicao pelo admin: o simples muda so nome e e-mail; o completo completa ou atualiza tudo
    Usuario editarSimples(long id, @Valid UsuarioEdicaoSimplesDTO edicao);
    Usuario editarCompleto(long id, @Valid UsuarioEdicaoCompletaDTO edicao);
    Usuario alternarAtivo(long id, String keycloakIdDoAdmin);
    Usuario findById(long id);
    UsuarioResumoResponseDTO buscarResumo(long id);
    // busca = parte do nome ou do email; idPlano = plano vigente da assinatura (ambos opcionais)
    // filtros da listagem do admin (todos opcionais): ativo nulo = todos; incompleto = cliente sem
    // documento, perfil fiscal ou endereco; agendamento = troca ou cancelamento agendado
    record Filtro(String busca, Long idPlano, Boolean ativo, boolean incompleto, boolean agendamento) {
    }

    List<Usuario> findByFiltros(Filtro filtro, int page, int pageSize);
    long count(Filtro filtro);

    // CSV da listagem com os mesmos filtros (sem CPF, telefone e endereco)
    String exportarCsv(Filtro filtro);

    // encerra as sessoes no Keycloak e invalida na hora os tokens ja emitidos
    Usuario encerrarSessoes(long id, String keycloakIdDoAdmin);

    // cadastro, mudancas de plano, pagamentos e acoes do admin, do mais recente para o mais antigo
    List<EventoLinhaDoTempoDTO> linhaDoTempo(long id);
}
