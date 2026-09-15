package br.com.campusgigs.domain;

import br.com.campusgigs.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContratacaoTest {

    private Usuario prestador;
    private Usuario contratante;
    private Servico servico;

    @BeforeEach
    void preparar() {
        prestador = new Usuario("Prestador", "prestador@x.com", "hash", Papel.USER);
        prestador.setId(1L);
        contratante = new Usuario("Contratante", "contratante@x.com", "hash", Papel.USER);
        contratante.setId(2L);
        servico = new Servico(prestador, "Logo", "Criacao de logo", "Design", new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Contratacao nasce SOLICITADA e congela o preco do servico")
    void nasceSolicitada() {
        Contratacao contratacao = new Contratacao(servico, contratante);

        assertThat(contratacao.getSituacao()).isEqualTo(SituacaoContratacao.SOLICITADA);
        assertThat(contratacao.getPrecoAcordado()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Fluxo feliz: SOLICITADA -> ACEITA -> CONCLUIDA")
    void fluxoFeliz() {
        Contratacao contratacao = new Contratacao(servico, contratante);

        contratacao.aceitar();
        assertThat(contratacao.getSituacao()).isEqualTo(SituacaoContratacao.ACEITA);

        contratacao.concluir();
        assertThat(contratacao.getSituacao()).isEqualTo(SituacaoContratacao.CONCLUIDA);
    }

    @Test
    @DisplayName("Nao e possivel concluir uma contratacao que ainda nao foi aceita")
    void naoConcluiSemAceitar() {
        Contratacao contratacao = new Contratacao(servico, contratante);

        assertThatThrownBy(contratacao::concluir)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Contratacao ja finalizada nao pode ser cancelada")
    void naoCancelaFinalizada() {
        Contratacao contratacao = new Contratacao(servico, contratante);
        contratacao.aceitar();
        contratacao.concluir();

        assertThatThrownBy(contratacao::cancelar)
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Identifica corretamente prestador e contratante")
    void identificaPartes() {
        Contratacao contratacao = new Contratacao(servico, contratante);

        assertThat(contratacao.ehPrestador(prestador)).isTrue();
        assertThat(contratacao.ehContratante(contratante)).isTrue();
        assertThat(contratacao.ehPrestador(contratante)).isFalse();
    }
}
