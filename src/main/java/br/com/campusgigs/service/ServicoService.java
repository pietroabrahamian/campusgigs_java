package br.com.campusgigs.service;

import br.com.campusgigs.domain.Servico;
import br.com.campusgigs.domain.SituacaoServico;
import br.com.campusgigs.domain.Usuario;
import br.com.campusgigs.dto.ServicoRequest;
import br.com.campusgigs.exception.OperacaoNaoPermitidaException;
import br.com.campusgigs.exception.RecursoNaoEncontradoException;
import br.com.campusgigs.repository.ServicoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final UsuarioService usuarioService;

    public ServicoService(ServicoRepository servicoRepository, UsuarioService usuarioService) {
        this.servicoRepository = servicoRepository;
        this.usuarioService = usuarioService;
    }

    /** Qualquer usuario autenticado pode publicar. */
    @Transactional
    public Servico publicar(Long prestadorId, ServicoRequest request) {
        Usuario prestador = usuarioService.buscarPorId(prestadorId);
        Servico servico = new Servico(
                prestador,
                request.titulo().trim(),
                request.descricao().trim(),
                request.categoria().trim(),
                request.preco());
        return servicoRepository.save(servico);
    }

    @Transactional(readOnly = true)
    public Page<Servico> listar(SituacaoServico situacao, String categoria, Pageable pageable) {
        boolean temCategoria = categoria != null && !categoria.isBlank();

        if (situacao != null && temCategoria) {
            return servicoRepository.findBySituacaoAndCategoriaIgnoreCase(situacao, categoria.trim(), pageable);
        }
        if (situacao != null) {
            return servicoRepository.findBySituacao(situacao, pageable);
        }
        if (temCategoria) {
            return servicoRepository.findByCategoriaIgnoreCase(categoria.trim(), pageable);
        }
        return servicoRepository.findAllBy(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Servico> listarDoPrestador(Long prestadorId, Pageable pageable) {
        return servicoRepository.findByPrestadorId(prestadorId, pageable);
    }

    @Transactional(readOnly = true)
    public Servico buscarPorId(Long id) {
        return servicoRepository.findWithPrestadorById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Servico", id));
    }

    /** Editar: somente o dono. Nem o ADMIN edita servico alheio. */
    @Transactional
    public Servico atualizar(Long servicoId, Long usuarioId, ServicoRequest request) {
        Servico servico = buscarPorId(servicoId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);
        exigirDono(servico, usuario, "editar");

        servico.atualizarDados(
                request.titulo().trim(),
                request.descricao().trim(),
                request.categoria().trim(),
                request.preco());
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico pausar(Long servicoId, Long usuarioId) {
        Servico servico = buscarPorId(servicoId);
        exigirDono(servico, usuarioService.buscarPorId(usuarioId), "pausar");
        servico.pausar();
        return servicoRepository.save(servico);
    }

    @Transactional
    public Servico reativar(Long servicoId, Long usuarioId) {
        Servico servico = buscarPorId(servicoId);
        exigirDono(servico, usuarioService.buscarPorId(usuarioId), "reativar");
        servico.reativar();
        return servicoRepository.save(servico);
    }

    /** Encerrar: o dono OU um ADMIN (moderacao da plataforma). */
    @Transactional
    public Servico encerrar(Long servicoId, Long usuarioId) {
        Servico servico = buscarPorId(servicoId);
        Usuario usuario = usuarioService.buscarPorId(usuarioId);

        if (!servico.pertenceA(usuario) && !usuario.isAdmin()) {
            throw new OperacaoNaoPermitidaException(
                    "Voce so pode encerrar os seus proprios servicos. Encerrar servicos de terceiros e exclusivo do ADMIN.");
        }

        servico.encerrar();
        return servicoRepository.save(servico);
    }

    private void exigirDono(Servico servico, Usuario usuario, String operacao) {
        if (!servico.pertenceA(usuario)) {
            throw new OperacaoNaoPermitidaException(
                    "Voce so pode " + operacao + " os seus proprios servicos.");
        }
    }
}
