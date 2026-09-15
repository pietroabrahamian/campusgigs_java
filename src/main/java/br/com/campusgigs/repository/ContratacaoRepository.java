package br.com.campusgigs.repository;

import br.com.campusgigs.domain.Contratacao;
import br.com.campusgigs.domain.SituacaoContratacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ContratacaoRepository extends JpaRepository<Contratacao, Long> {

    @EntityGraph(attributePaths = {"servico", "servico.prestador", "contratante"})
    Optional<Contratacao> findWithDetalhesById(Long id);

    /** Contratacoes em que o usuario e o contratante OU o prestador do servico. */
    @EntityGraph(attributePaths = {"servico", "servico.prestador", "contratante"})
    @Query("SELECT c FROM Contratacao c WHERE c.contratante.id = :usuarioId OR c.servico.prestador.id = :usuarioId")
    Page<Contratacao> buscarDoUsuario(@Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Impede o mesmo usuario de abrir duas contratacoes em aberto para o mesmo servico. */
    boolean existsByServicoIdAndContratanteIdAndSituacaoIn(Long servicoId,
                                                           Long contratanteId,
                                                           Collection<SituacaoContratacao> situacoes);
}
