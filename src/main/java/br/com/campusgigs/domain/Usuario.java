package br.com.campusgigs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    /** Sempre o hash BCrypt. Nunca a senha em texto puro. */
    @Column(name = "senha", nullable = false, length = 200)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 20)
    private Papel papel;

    @Embedded
    private Endereco endereco;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Usuario(String nome, String email, String senha, Papel papel) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public boolean isAdmin() {
        return papel == Papel.ADMIN;
    }

    public boolean mesmoQue(Usuario outro) {
        return outro != null && Objects.equals(this.id, outro.getId());
    }
}
