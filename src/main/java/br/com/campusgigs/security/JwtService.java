package br.com.campusgigs.security;

import br.com.campusgigs.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Emissao e validacao dos tokens de acesso (JWT assinado com HS256).
 *
 * O token carrega o e-mail no subject e o papel como claim. Nada de senha
 * ou dado sensivel vai no payload - JWT e assinado, nao criptografado.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    public static final String CLAIM_PAPEL = "papel";

    private final SecretKey chave;
    private final long expiracaoMinutos;
    private final String emissor;

    public JwtService(@Value("${campusgigs.jwt.secret}") String secret,
                      @Value("${campusgigs.jwt.expiracao-minutos}") long expiracaoMinutos,
                      @Value("${campusgigs.jwt.emissor}") String emissor) {

        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "campusgigs.jwt.secret precisa ter no minimo 32 bytes para assinar em HS256.");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.expiracaoMinutos = expiracaoMinutos;
        this.emissor = emissor;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(expiracaoMinutos, ChronoUnit.MINUTES);

        return Jwts.builder()
                .issuer(emissor)
                .subject(usuario.getEmail())
                .claim(CLAIM_PAPEL, usuario.getPapel().name())
                .claim("uid", usuario.getId())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
    }

    public Instant expiracaoDe(String token) {
        return lerClaims(token).getExpiration().toInstant();
    }

    public long getExpiracaoMinutos() {
        return expiracaoMinutos;
    }

    /** Devolve o e-mail (subject) ou null se o token for invalido/expirado. */
    public String extrairEmailSeValido(String token) {
        try {
            return lerClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado: {}", e.getMessage());
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token invalido: {}", e.getMessage());
            return null;
        }
    }

    private Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .requireIssuer(emissor)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
