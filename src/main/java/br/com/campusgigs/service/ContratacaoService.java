package br.com.campusgigs.service;

import br.com.campusgigs.domain.Contratacao;
import br.com.campusgigs.domain.Servico;
import br.com.campusgigs.domain.SituacaoContratacao;
import br.com.campusgigs.domain.Usuario;
import br.com.campusgigs.exception.OperacaoNaoPermitidaException;
import br.com.campusgigs.exception.RecursoNaoEncontradoException;
import br.com.campusgigs.exception.RegraDeNegocioException;
import br.com.campusgigs.repository.ContratacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContratacaoService {

    private static final List<SituacaoContratacao> EM_ABERTO =
            List.of(SituacaoContratacao.SOLICITADA, SituacaoContratacao.ACEITA);

    private final ContratacaoRepository contratacaoRepository;
    private final ServicoService servicoService;
    private final UsuarioService usuarioService;

    public ContratacaoService(ContratacaoRepository contratacaoRepository,
                              ServicoService servicoService,
                              UsuarioService usuarioService) {
        this.contratacaoRepository = contratacaoRepository;
        this.servicoService = servicoService;
        this.usuarioService = usuarioService;
    }

    /**
     * Regras de contratacao:
     *  - so servico ATIVO pode ser contratado;
     *  - ninguem contrata o proprio servico;
     *  - nao se abre uma segunda contratacao do mesmo servico enquanto houver uma em aberto.
     */
    @Transactional
    public Contratacao contratar(Long servicoId, Long contratanteId) {
        Servico servico = servicoService.buscarPorId(servicoId);
        Usuario contratante = usuarioService.buscarPorId(contratanteId);

        if (servico.pertenceA(contratante)) {
            throw new OperacaoNaoPermitidaException("Voce nao pode contratar o seu proprio servico.");
        }

        if (!servico.estaAtivo()) {
            throw new RegraDeNegocioException(
                    "Este servico esta " + servico.getSituacao() + " e nao pode ser contratado.");
        }

        boolean jaTemEmAberto = contratacaoRepository
                .existsByServicoIdAndContratanteIdAndSituacaoIn(servicoId, contratanteId, EM_ABERTO);
        if (jaTemEmAberto) {
            throw new RegraDeNegocioException("Voce ja possui uma contratacao em aberto para este servico.");
        }

        return contratacaoRepository.save(new Contratacao(servico, contratante));
    }

    @Transactional(readOnly = true)
    public Contratacao buscarVisivelPara(Long contratacaoId, Long usuarioId) {
        Contratacao contratacao = buscar(contratacaoId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        if (!contratacao.ehContratante(usuario) && !contratacao.ehPrestador(usuario) && !usuario.isAdmin()) {
            throw new OperacaoNaoPermitidaException("Esta contratacao nao pertence a voce.");
        }
        return contratacao;
    }

    @Transactional(readOnly = true)
    public Page<Contratacao> listarDoUsuario(Long usuarioId, Pageable pageable) {
        return contratacaoRepository.buscarDoUsuario(usuarioId, pageable);
    }

    /** Aceitar: apenas o prestador do servico. */
    @Transactional
    public Contratacao aceitar(Long contratacaoId, Long usuarioId) {
        Contratacao contratacao = buscar(contratacaoId);
        exigirPrestador(contratacao, usuarioId, "aceitar");
        contratacao.aceitar();
        return contratacaoRepository.save(contratacao);
    }

    /** Concluir: apenas o prestador do servico. */
    @Transactional
    public Contratacao concluir(Long contratacaoId, Long usuarioId) {
        Contratacao contratacao = buscar(contratacaoId);
        exigirPrestador(contratacao, usuarioId, "concluir");
        contratacao.concluir();
        return contratacaoRepository.save(contratacao);
    }

    /** Cancelar: contratante, prestador ou ADMIN. */
    @Transactional
    public Contratacao cancelar(Long contratacaoId, Long usuarioId) {
        Contratacao contratacao = buscar(contratacaoId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        if (!contratacao.ehContratante(usuario) && !contratacao.ehPrestador(usuario) && !usuario.isAdmin()) {
            throw new OperacaoNaoPermitidaException(
                    "Apenas o contratante, o prestador ou um ADMIN podem cancelar esta contratacao.");
        }

        contratacao.cancelar();
        return contratacaoRepository.save(contratacao);
    }

    private Contratacao buscar(Long id) {
        return contratacaoRepository.findWithDetalhesById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Contratacao", id));
    }

    private void exigirPrestador(Contratacao contratacao, Long usuarioId, String operacao) {
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        if (!contratacao.ehPrestador(usuario)) {
            throw new OperacaoNaoPermitidaException(
                    "Apenas o prestador do servico pode " + operacao + " esta contratacao.");
        }
    }
}
