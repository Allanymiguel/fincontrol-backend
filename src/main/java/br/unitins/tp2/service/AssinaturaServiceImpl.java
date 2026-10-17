package br.unitins.tp2.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import br.unitins.tp2.dto.AssinaturaRequestDTO;
import br.unitins.tp2.dto.AssinaturaUpdateDTO;
import br.unitins.tp2.exception.ResourceNotFoundException;
import br.unitins.tp2.exception.ValidationException;
import br.unitins.tp2.model.Assinatura;
import br.unitins.tp2.model.Cupom;
import br.unitins.tp2.model.FormaPagamento;
import br.unitins.tp2.model.Periodicidade;
import br.unitins.tp2.model.Plano;
import br.unitins.tp2.model.StatusAssinatura;
import br.unitins.tp2.model.Usuario;
import br.unitins.tp2.repository.AssinaturaRepository;
import br.unitins.tp2.repository.CupomRepository;
import br.unitins.tp2.repository.PlanoRepository;
import br.unitins.tp2.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AssinaturaServiceImpl implements AssinaturaService {

    private static final BigDecimal CEM = new BigDecimal("100");

    @Inject
    AssinaturaRepository assinaturaRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PlanoRepository planoRepository;

    @Inject
    CupomRepository cupomRepository;

    // Nova assinatura comeca ATIVA hoje, sem data de fim. O valor pago e calculado aqui.
    // Se o usuario ja tem uma ATIVA (ex.: o Gratuito do cadastro), ela e encerrada como CANCELADA
    // hoje, na mesma transacao: assim o upgrade/troca de plano e um passo so e continua havendo
    // uma unica ATIVA por usuario. Todas as validacoes vem antes, para nada mudar se falhar.
    @Override
    @Transactional
    public Assinatura create(AssinaturaRequestDTO dto) {
        Usuario usuario = buscarUsuarioOuFalhar(dto.idUsuario());
        Plano plano = buscarPlanoOuFalhar(dto.idPlano());
        Cupom cupom = dto.idCupom() == null ? null : buscarCupomValidoOuFalhar(dto.idCupom());
        Periodicidade periodicidade = buscarPeriodicidadeOuFalhar(dto.idPeriodicidade());
        FormaPagamento formaPagamento = buscarFormaPagamentoOuFalhar(dto.idFormaPagamento());

        Assinatura anterior = assinaturaRepository.findAtivaDoUsuarioExceptId(usuario.getId(), null);
        if (anterior != null && anterior.getPlano().getId().equals(plano.getId())
                && anterior.getPeriodicidade() == periodicidade)
            throw ValidationException.of("idPlano", "O usuário já tem este plano ativo nessa periodicidade.");

        // "Nenhuma" so vale para assinatura sem custo (plano Gratuito ou cupom de 100%)
        BigDecimal valorPago = calcularValorPago(plano, periodicidade, cupom);
        if (formaPagamento == FormaPagamento.NENHUMA && valorPago.signum() > 0)
            throw ValidationException.of("idFormaPagamento",
                    "A forma de pagamento \"Nenhuma\" só vale para assinaturas sem custo.");

        if (anterior != null)
            substituir(anterior, plano, periodicidade);

        Assinatura nova = new Assinatura();
        nova.setUsuario(usuario);
        nova.setPlano(plano);
        nova.setCupom(cupom);
        nova.setPeriodicidade(periodicidade);
        nova.setFormaPagamento(formaPagamento);
        nova.setValorPago(valorPago);
        nova.setStatus(StatusAssinatura.ATIVA);
        nova.setDataInicio(LocalDate.now());
        nova.setDataFim(null);
        nova.setObservacao(vazioParaNull(dto.observacao()));

        assinaturaRepository.persist(nova);

        return nova;
    }

    // Altera so status, data de fim e observacao. Encerrar (CANCELADA/EXPIRADA) sem data de fim
    // registra hoje como fim.
    @Override
    @Transactional
    public void update(long id, AssinaturaUpdateDTO dto) {
        Assinatura assinatura = buscarAssinaturaOuFalhar(id);
        StatusAssinatura status = buscarStatusOuFalhar(dto.idStatus());
        LocalDate dataFim = dto.dataFim();

        if (dataFim != null && dataFim.isBefore(assinatura.getDataInicio()))
            throw ValidationException.of("dataFim", "A data de fim não pode ser anterior ao início da assinatura.");

        if (status == StatusAssinatura.ATIVA) {
            if (dataFim != null && dataFim.isBefore(LocalDate.now()))
                throw ValidationException.of("dataFim", "Uma assinatura ativa não pode ter data de fim no passado.");
            validarUnicaAtiva(assinatura.getUsuario().getId(), assinatura.getId());
        }

        if (dataFim == null && isEncerrada(status))
            dataFim = LocalDate.now();

        assinatura.setStatus(status);
        assinatura.setDataFim(dataFim);
        assinatura.setObservacao(vazioParaNull(dto.observacao()));
    }

    // "Excluir" uma assinatura e cancela-la: o historico do usuario e mantido
    @Override
    @Transactional
    public Assinatura cancelar(long id) {
        Assinatura assinatura = buscarAssinaturaOuFalhar(id);
        if (assinatura.getStatus() == StatusAssinatura.CANCELADA)
            throw ValidationException.of("status", "A assinatura já está cancelada.");

        assinatura.setStatus(StatusAssinatura.CANCELADA);
        encerrarHoje(assinatura);

        return assinatura;
    }

    // encerra a assinatura ativa que esta sendo trocada por outra, registrando o motivo
    private void substituir(Assinatura anterior, Plano novoPlano, Periodicidade periodicidade) {
        anterior.setStatus(StatusAssinatura.CANCELADA);
        encerrarHoje(anterior);
        anterior.setObservacao(anexarObservacao(anterior.getObservacao(),
                "Substituída pelo plano " + novoPlano.getNome() + " (" + periodicidade.getNome().toLowerCase() + ")"));
    }

    // termina hoje (ou antes, se ja tinha um fim anterior)
    private void encerrarHoje(Assinatura assinatura) {
        LocalDate hoje = LocalDate.now();
        if (assinatura.getDataFim() == null || assinatura.getDataFim().isAfter(hoje))
            assinatura.setDataFim(hoje.isBefore(assinatura.getDataInicio()) ? assinatura.getDataInicio() : hoje);
    }

    // mantem a observacao do admin e acrescenta a nota, sem passar do limite da coluna
    private String anexarObservacao(String atual, String nota) {
        if (atual == null || atual.isBlank())
            return nota;

        String junta = atual + " | " + nota;
        return junta.length() <= 255 ? junta : atual;
    }

    @Override
    public Assinatura findById(long id) {
        return buscarAssinaturaOuFalhar(id);
    }

    @Override
    public List<Assinatura> findByFiltros(String busca, Long idStatus, int page, int pageSize) {
        return assinaturaRepository.findByFiltros(busca, buscarStatusDoFiltro(idStatus)).page(page, pageSize).list();
    }

    @Override
    public long count(String busca, Long idStatus) {
        return assinaturaRepository.findByFiltros(busca, buscarStatusDoFiltro(idStatus)).count();
    }

    // preco do plano na periodicidade, menos o percentual do cupom; arredondado em centavos
    static BigDecimal calcularValorPago(Plano plano, Periodicidade periodicidade, Cupom cupom) {
        BigDecimal preco = periodicidade == Periodicidade.ANUAL ? plano.getPrecoAnual() : plano.getPrecoMensal();
        if (cupom == null)
            return preco.setScale(2, RoundingMode.HALF_UP);

        BigDecimal fatorPago = CEM.subtract(cupom.getPercentualDesconto()).divide(CEM);
        return preco.multiply(fatorPago).setScale(2, RoundingMode.HALF_UP);
    }

    // RN: o usuario pode ter varias assinaturas, mas so uma ATIVA por vez.
    // Usada ao reativar uma antiga (PUT); na criacao a ativa anterior e substituida.
    private void validarUnicaAtiva(Long idUsuario, Long idAssinatura) {
        if (assinaturaRepository.findAtivaDoUsuarioExceptId(idUsuario, idAssinatura) != null)
            throw ValidationException.of("idUsuario",
                    "Este usuário já tem uma assinatura ativa. Cancele-a ou encerre-a antes.");
    }

    private boolean isEncerrada(StatusAssinatura status) {
        return status == StatusAssinatura.CANCELADA || status == StatusAssinatura.EXPIRADA;
    }

    private Usuario buscarUsuarioOuFalhar(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario);
        if (usuario == null)
            throw ValidationException.of("idUsuario", "Usuário não encontrado.");
        if (!usuario.getAtivo())
            throw ValidationException.of("idUsuario", "Usuário desativado não pode receber assinatura.");

        return usuario;
    }

    private Plano buscarPlanoOuFalhar(Long idPlano) {
        Plano plano = planoRepository.findById(idPlano);
        if (plano == null)
            throw ValidationException.of("idPlano", "Plano não encontrado.");
        if (!plano.getAtivo())
            throw ValidationException.of("idPlano", "Este plano está inativo e não aceita novas assinaturas.");

        return plano;
    }

    // so cupom ativo e dentro da validade (o ultimo dia de validade ainda vale)
    private Cupom buscarCupomValidoOuFalhar(Long idCupom) {
        Cupom cupom = cupomRepository.findById(idCupom);
        if (cupom == null)
            throw ValidationException.of("idCupom", "Cupom não encontrado.");
        if (!cupom.getAtivo())
            throw ValidationException.of("idCupom", "Este cupom está inativo.");
        if (cupom.getValidade().isBefore(LocalDate.now()))
            throw ValidationException.of("idCupom", "Este cupom está expirado.");

        return cupom;
    }

    private Periodicidade buscarPeriodicidadeOuFalhar(Long idPeriodicidade) {
        Periodicidade periodicidade = Periodicidade.valueOf(idPeriodicidade);
        if (periodicidade == null)
            throw ValidationException.of("idPeriodicidade", "Periodicidade não encontrada.");

        return periodicidade;
    }

    private FormaPagamento buscarFormaPagamentoOuFalhar(Long idFormaPagamento) {
        FormaPagamento forma = FormaPagamento.valueOf(idFormaPagamento);
        if (forma == null)
            throw ValidationException.of("idFormaPagamento", "Forma de pagamento não encontrada.");

        return forma;
    }

    private StatusAssinatura buscarStatusOuFalhar(Long idStatus) {
        StatusAssinatura status = StatusAssinatura.valueOf(idStatus);
        if (status == null)
            throw ValidationException.of("idStatus", "Status de assinatura não encontrado.");

        return status;
    }

    // no filtro da listagem o status e opcional, mas se vier precisa existir
    private StatusAssinatura buscarStatusDoFiltro(Long idStatus) {
        return idStatus == null ? null : buscarStatusOuFalhar(idStatus);
    }

    private Assinatura buscarAssinaturaOuFalhar(long id) {
        Assinatura assinatura = assinaturaRepository.findById(id);
        if (assinatura == null)
            throw new ResourceNotFoundException("Assinatura não encontrada.");

        return assinatura;
    }

    private String vazioParaNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
