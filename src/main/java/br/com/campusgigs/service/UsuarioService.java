package br.com.campusgigs.service;

import br.com.campusgigs.domain.Papel;
import br.com.campusgigs.domain.Usuario;
import br.com.campusgigs.dto.RegistroRequest;
import br.com.campusgigs.exception.RecursoNaoEncontradoException;
import br.com.campusgigs.exception.RegraDeNegocioException;
import br.com.campusgigs.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnderecoService enderecoService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          EnderecoService enderecoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.enderecoService = enderecoService;
    }

    /**
     * Cadastro publico: todo mundo entra como USER.
     * ADMIN so existe via migration/seed - nao se promove pela API.
     */
    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraDeNegocioException("Ja existe um usuario cadastrado com o e-mail " + email + ".");
        }

        Usuario usuario = new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                Papel.USER);

        if (request.cep() != null && !request.cep().isBlank()) {
            usuario.setEndereco(enderecoService.resolverPorCep(request.cep()));
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Usuario", id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado: " + email));
    }

    /** Atualiza o CEP e, com ele, cidade e UF vindas do servico externo. */
    @Transactional
    public Usuario atualizarCep(Long usuarioId, String cep) {
        Usuario usuario = buscarPorId(usuarioId);
        usuario.setEndereco(enderecoService.resolverPorCep(cep));
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listarTodos(Pageable pageable) {
        return usuarioRepository.findAllByOrderByIdAsc(pageable);
    }
}
