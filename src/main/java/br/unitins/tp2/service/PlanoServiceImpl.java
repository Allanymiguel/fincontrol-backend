package br.unitins.tp2.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import br.unitins.tp2.dto.PlanoRequestDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.Problem;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.AcaoAuditoria;
import br.unitins.tp2.model.ArquivoArmazenado;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.TipoAlvoAuditoria;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.CupomRepository;
import br.unitins.tp2.repository.PagamentoRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.util.RegrasImagem;
import br.unitins.tp2.util.RetratoAuditoria;
import br.unitins.tp2.util.TipoImagem;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PlanoServiceImpl implements PlanoService {

    @Inject
    PlanoRepository planoRepository;

    @Inject
    AuditoriaService auditoria;

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    PagamentoRepository pagamentoRepository;

    @Inject
    CupomRepository cupomRepository;

    @Inject
    ArmazenamentoService armazenamento;

    // resultado da transacao do envio: o plano gravado e a chave que ele tinha antes (para apagar depois)
    private record ImagemTrocada(Plano plano, String chaveAntiga) {
    }

    @Override
    @Transactional
    public Plano create(PlanoRequestDTO plano) {
        validarDados(plano, null);

        Plano novoPlano = new Plano();
        aplicarDados(novoPlano, plano);

        planoRepository.persist(novoPlano);
        auditoria.registrar(AcaoAuditoria.CRIACAO_PLANO, TipoAlvoAuditoria.PLANO, novoPlano.getId(),
                novoPlano.getNome(), Map.of(), RetratoAuditoria.de(novoPlano));

        return novoPlano;
    }

    @Override
    @Transactional
    public void update(long id, PlanoRequestDTO plano) {
        Plano edicaoPlano = buscarPlanoOuFalhar(id);
        validarDados(plano, id);
        Map<String, String> antes = RetratoAuditoria.de(edicaoPlano);
        aplicarDados(edicaoPlano, plano);
        auditoria.registrar(AcaoAuditoria.ALTERACAO_PLANO, TipoAlvoAuditoria.PLANO, id,
                edicaoPlano.getNome(), antes, RetratoAuditoria.de(edicaoPlano));
    }

    // plano em uso (vigente ou agendado) ou no historico de pagamentos nao pode ser excluido: o admin o desativa
    @Override
    @Transactional
    public void delete(long id) {
        // a mensagem vai no detail para aparecer no aviso do front (nao ha campo ligado a exclusao)
        if (assinaturaRepository.countByPlano(id) > 0 || pagamentoRepository.countByPlano(id) > 0) {
            String mensagem = "Este plano tem assinaturas e não pode ser excluído. Desative-o para não aceitar novas.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("ativo", mensagem)));
        }
        if (cupomRepository.countByPlano(id) > 0) {
            String mensagem = "Há cupons restritos a este plano. Tire o plano desses cupons antes de excluí-lo.";
            throw new ValidationException(mensagem, List.of(new Problem.FieldError("ativo", mensagem)));
        }

        Plano excluido = buscarPlanoOuFalhar(id);
        Map<String, String> antes = RetratoAuditoria.de(excluido);
        planoRepository.delete(excluido);
        auditoria.registrar(AcaoAuditoria.EXCLUSAO_PLANO, TipoAlvoAuditoria.PLANO, id,
                excluido.getNome(), antes, Map.of());
    }

    @Override
    public Plano findById(long id) {
        return buscarPlanoOuFalhar(id);
    }

    @Override
    public List<Plano> findAll(int page, int pageSize) {
        return planoRepository.findAll().page(page, pageSize).list();
    }

    @Override
    public List<Plano> findByNome(String nome, int page, int pageSize) {
        return planoRepository.findByNome(nome).page(page, pageSize).list();
    }

    @Override
    public long count() {
        return planoRepository.findAll().count();
    }

    @Override
    public long count(String nome) {
        return planoRepository.findByNome(nome).count();
    }

    // S3 fora da transacao (nenhuma conexao do banco fica presa esperando o armazenamento):
    // valida -> confere o plano -> envia o objeto novo -> grava os metadados (com lock) -> apaga o antigo.
    // Se a gravacao falhar, apaga o objeto novo; se apagar o antigo falhar, ele so fica orfao no bucket.
    @Override
    public Plano enviarImagem(long id, String nomeArquivo, long tamanho, Path arquivo) {
        RegrasImagem.validarTamanho(tamanho);
        byte[] conteudo = ler(arquivo);
        TipoImagem tipo = RegrasImagem.validar(conteudo);

        boolean existe = QuarkusTransaction.requiringNew().call(() -> planoRepository.findById(id) != null);
        if (!existe)
            throw new ResourceNotFoundException("Plano não encontrado.");

        String chaveNova = RegrasImagem.chave(id, tipo);
        armazenamento.enviar(chaveNova, conteudo, tipo.getContentType());

        ImagemTrocada troca;
        try {
            troca = QuarkusTransaction.requiringNew().call(() -> {
                Plano plano = buscarPlanoParaAlterar(id);
                // lida depois do lock: e a chave que ESTE envio substitui
                String chaveAntiga = plano.getImagem() == null ? null : plano.getImagem().getChave();
                Map<String, String> antes = RetratoAuditoria.de(plano);
                plano.setImagem(new ArquivoArmazenado(chaveNova, RegrasImagem.nomeParaDownload(nomeArquivo, tipo),
                        tipo.getContentType(), conteudo.length, LocalDateTime.now()));
                auditoria.registrar(AcaoAuditoria.ENVIO_IMAGEM_PLANO, TipoAlvoAuditoria.PLANO, id,
                        plano.getNome(), antes, RetratoAuditoria.de(plano));
                return new ImagemTrocada(plano, chaveAntiga);
            });
        } catch (RuntimeException e) {
            armazenamento.excluirSemFalhar(chaveNova);
            throw e;
        }

        if (troca.chaveAntiga() != null)
            armazenamento.excluirSemFalhar(troca.chaveAntiga());
        return troca.plano();
    }

    private static byte[] ler(Path arquivo) {
        try {
            return Files.readAllBytes(arquivo);
        } catch (IOException | RuntimeException e) {
            throw ValidationException.of(RegrasImagem.CAMPO, "Não foi possível ler o arquivo.");
        }
    }

    private Plano buscarPlanoParaAlterar(long id) {
        Plano plano = planoRepository.findByIdParaAlterar(id);
        if (plano == null)
            throw new ResourceNotFoundException("Plano não encontrado.");
        return plano;
    }

    // Regras que a Bean Validation (anotacoes do DTO) nao consegue checar sozinha
    private void validarDados(PlanoRequestDTO dto, Long idPlano) {
        if (planoRepository.findByNomeExceptId(dto.nome().trim(), idPlano) != null)
            throw ValidationException.of("nome", "Já existe um plano cadastrado com esse nome.");
    }

    private void aplicarDados(Plano destino, PlanoRequestDTO origem) {
        destino.setNome(origem.nome().trim());
        destino.setPrecoMensal(origem.precoMensal());
        destino.setPrecoAnual(origem.precoAnual());
        destino.setLimiteTransacoes(origem.limiteTransacoes());
        destino.setLimiteMetas(origem.limiteMetas());
        destino.setLiberaRecorrencias(origem.liberaRecorrencias());
        destino.setLiberaFiscal(origem.liberaFiscal());
        destino.setLiberaExportacao(origem.liberaExportacao());
        destino.setAtivo(origem.ativo());
    }

    private Plano buscarPlanoOuFalhar(long id) {
        Plano plano = planoRepository.findById(id);
        if (plano == null)
            throw new ResourceNotFoundException("Plano não encontrado.");

        return plano;
    }
}
