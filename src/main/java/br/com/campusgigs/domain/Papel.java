package br.com.campusgigs.domain;

/**
 * Papel do usuario dentro da plataforma.
 * O prefixo ROLE_ e adicionado na camada de seguranca (UsuarioAutenticado).
 */
public enum Papel {
    ADMIN,
    USER
}
