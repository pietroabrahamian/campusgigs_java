package br.com.campusgigs.domain;

import br.com.campusgigs.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicoTest {

    private Usuario usuario(long id, String email) {
        Usuario usuario = new Usuario("Aluno " + id, email, "hash", Papel.USER);
        usuario.setId(id);
        return usuario;
    }

    private Servico servicoDe(Usuario prestador) {
        return new Servico(prestador, "Monitoria de Java", "Aulas de POO", "Educacao", new BigDecimal("80.00"));
    }

    @Test
    @DisplayName("Servico nasce ATIVO")
    void nasceAtivo() {
        assertThat(servicoDe(usuario(1L, "a@x.com")).getSituacao()).isEqualTo(SituacaoServico.ATIVO);
    }

    @Test
    @DisplayName("Servico pertence apenas ao prestador que o publicou")
    void pertenceApenasAoDono() {
        Usuario dono = usuario(1L, "dono@x.com");
        Usuario outro = usuario(2L, "outro@x.com");
        Servico servico = servicoDe(dono);

        assertThat(servico.pertenceA(dono)).isTrue();
        assertThat(servico.pertenceA(outro)).isFalse();
    }

    @Test
    @DisplayName("Servico encerrado nao volta atras")
    void encerradoEhTerminal() {
        Servico servico = servicoDe(usuario(1L, "dono@x.com"));
        servico.encerrar();

        assertThat(servico.estaEncerrado()).isTrue();
        assertThatThrownBy(servico::reativar).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(servico::encerrar).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @DisplayName("Servico pausado nao esta ativo e por isso nao pode ser contratado")
    void pausadoNaoEstaAtivo() {
        Servico servico = servicoDe(usuario(1L, "dono@x.com"));
        servico.pausar();

        assertThat(servico.estaAtivo()).isFalse();
        assertThat(servico.getSituacao()).isEqualTo(SituacaoServico.PAUSADO);
    }
}
