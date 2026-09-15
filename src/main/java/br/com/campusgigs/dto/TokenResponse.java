package br.com.campusgigs.dto;

import java.time.Instant;

/** Token de acesso devolvido no login. */
public record TokenResponse(
        String token,
        String tipo,
        Instant expiraEm,
        Long usuarioId,
        String nome,
        String papel
) {
}
