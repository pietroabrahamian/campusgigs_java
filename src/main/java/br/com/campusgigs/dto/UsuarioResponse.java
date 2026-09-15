package br.com.campusgigs.dto;

import br.com.campusgigs.domain.Usuario;

import java.time.LocalDateTime;

/** Nunca expoe a senha (nem o hash). */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String papel,
        EnderecoResponse endereco,
        LocalDateTime criadoEm
) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPapel().name(),
                EnderecoResponse.de(usuario.getEndereco()),
                usuario.getCriadoEm());
    }
}
