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
@Table(name = "contratacao")
@Getter
@Setter
@NoArgsConstructor
public class Contratacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /** Quem contratou o freela. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contratante_id", nullable = false)
    private Usuario contratante;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoContratacao situacao;

    /** Congela o preco praticado no momento da contratacao. */
    @Column(name = "preco_acordado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAcordado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public Contratacao(Servico servico, Usuario contratante) {
        this.servico = servico;
        this.contratante = contratante;
        this.precoAcordado = servico.getPreco();
        this.situacao = SituacaoContratacao.SOLICITADA;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (situacao == null) {
            situacao = SituacaoContratacao.SOLICITADA;
        }
    }

    public boolean ehContratante(Usuario usuario) {
        return usuario != null && contratante.getId().equals(usuario.getId());
    }

    public boolean ehPrestador(Usuario usuario) {
        return usuario != null && servico.getPrestador().getId().equals(usuario.getId());
    }

    public void aceitar() {
        exigirSituacao(SituacaoContratacao.SOLICITADA, "aceita");
        trocarPara(SituacaoContratacao.ACEITA);
    }

    public void concluir() {
        exigirSituacao(SituacaoContratacao.ACEITA, "concluida");
        trocarPara(SituacaoContratacao.CONCLUIDA);
    }

    public void cancelar() {
        if (situacao.finalizada()) {
            throw new RegraDeNegocioException(
                    "Contratacao " + situacao + " nao pode ser cancelada.");
        }
        trocarPara(SituacaoContratacao.CANCELADA);
    }

    private void exigirSituacao(SituacaoContratacao esperada, String acao) {
        if (situacao != esperada) {
            throw new RegraDeNegocioException(
                    "Apenas uma contratacao " + esperada + " pode ser " + acao
                            + ". Situacao atual: " + situacao + ".");
        }
    }

    private void trocarPara(SituacaoContratacao nova) {
        this.situacao = nova;
        this.atualizadoEm = LocalDateTime.now();
    }
}
