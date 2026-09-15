package br.com.campusgigs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Le o header Authorization, valida o token e coloca o usuario no SecurityContext.
 *
 * Se o token for invalido o filtro nao autentica e deixa a requisicao seguir:
 * quem decide o status final e o SecurityConfig (401 no entry point) ou a
 * regra de autorizacao (403).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERRO = "campusgigs.erro.autenticacao";
    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(PREFIXO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIXO.length()).trim();
        String email = jwtService.extrairEmailSeValido(token);

        if (email == null) {
            request.setAttribute(ATRIBUTO_ERRO, "Token ausente, invalido ou expirado.");
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails usuario = usuarioDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (UsernameNotFoundException e) {
                // Token assinado por nos, mas o usuario nao existe mais.
                SecurityContextHolder.clearContext();
                request.setAttribute(ATRIBUTO_ERRO, "Usuario do token nao existe mais.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
