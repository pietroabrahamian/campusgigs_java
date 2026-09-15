package br.com.campusgigs.controller;

import br.com.campusgigs.domain.Usuario;
import br.com.campusgigs.dto.LoginRequest;
import br.com.campusgigs.dto.RegistroRequest;
import br.com.campusgigs.dto.TokenResponse;
import br.com.campusgigs.dto.UsuarioResponse;
import br.com.campusgigs.security.JwtService;
import br.com.campusgigs.security.UsuarioAutenticado;
import br.com.campusgigs.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Cadastro e autenticacao: os unicos endpoints publicos de escrita da API. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** POST /auth/registrar - cria um usuario com papel USER. */
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody @Valid RegistroRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        Usuario usuario = usuarioService.registrar(request);
        URI uri = uriBuilder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(UsuarioResponse.de(usuario));
    }

    /** POST /auth/login - devolve o token de acesso usado nas demais requisicoes. */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        // Credenciais invalidas -> BadCredentialsException -> 401 no GlobalExceptionHandler
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.senha()));

        UsuarioAutenticado autenticado = (UsuarioAutenticado) autenticacao.getPrincipal();
        Usuario usuario = autenticado.getUsuario();

        String token = jwtService.gerarToken(usuario);
        Instant expiraEm = Instant.now().plus(jwtService.getExpiracaoMinutos(), ChronoUnit.MINUTES);

        return ResponseEntity.ok(new TokenResponse(
                token,
                "Bearer",
                expiraEm,
                usuario.getId(),
                usuario.getNome(),
                usuario.getPapel().name()));
    }
}
