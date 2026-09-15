package br.com.campusgigs.dto;

import br.com.campusgigs.domain.Usuario;

public record UsuarioResumo(Long id, String nome, String email) {

    public static UsuarioResumo de(Usuario usuario) {
        return new UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
