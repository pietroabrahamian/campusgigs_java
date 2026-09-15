package br.com.campusgigs.domain;

import br.com.campusgigs.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servico")
@Getter
@Setter
@NoArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quem publicou o freela. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Usuario prestador;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 1000)
    private String descricao;

    @Column(name = "categoria", nullable = false, length = 60)
    private String categoria;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoServico situacao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Servico(Usuario prestador, String titulo, String descricao, String categoria, BigDecimal preco) {
        this.prestador = prestador;
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.situacao = SituacaoServico.ATIVO;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (situacao == null) {
            situacao = SituacaoServico.ATIVO;
        }
    }

    public boolean estaAtivo() {
        return situacao == SituacaoServico.ATIVO;
    }

    public boolean estaEncerrado() {
        return situacao == SituacaoServico.ENCERRADO;
    }

    /** Regra: so o dono (ou um ADMIN) opera sobre o servico. */
    public boolean pertenceA(Usuario usuario) {
        return usuario != null && prestador != null && usuario.getId().equals(prestador.getId());
    }

    public void atualizarDados(String titulo, String descricao, String categoria, BigDecimal preco) {
        garantirNaoEncerrado("editar");
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
    }

    public void pausar() {
        garantirNaoEncerrado("pausar");
        this.situacao = SituacaoServico.PAUSADO;
    }

    public void reativar() {
        garantirNaoEncerrado("reativar");
        this.situacao = SituacaoServico.ATIVO;
    }

    public void encerrar() {
        garantirNaoEncerrado("encerrar");
        this.situacao = SituacaoServico.ENCERRADO;
    }

    private void garantirNaoEncerrado(String operacao) {
        if (estaEncerrado()) {
            throw new RegraDeNegocioException(
                    "Nao e possivel " + operacao + " um servico ja encerrado.");
        }
    }
}
