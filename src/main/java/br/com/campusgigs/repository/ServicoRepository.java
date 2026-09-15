package br.com.campusgigs.repository;

import br.com.campusgigs.domain.Servico;
import br.com.campusgigs.domain.SituacaoServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * O @EntityGraph traz o prestador junto na mesma consulta,
 * evitando o N+1 na listagem (o relacionamento e LAZY por padrao).
 */
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @EntityGraph(attributePaths = "prestador")
    Page<Servico> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = "prestador")
    Page<Servico> findBySituacao(SituacaoServico situacao, Pageable pageable);

    @EntityGraph(attributePaths = "prestador")
    Page<Servico> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    @EntityGraph(attributePaths = "prestador")
    Page<Servico> findBySituacaoAndCategoriaIgnoreCase(SituacaoServico situacao, String categoria, Pageable pageable);

    @EntityGraph(attributePaths = "prestador")
    Page<Servico> findByPrestadorId(Long prestadorId, Pageable pageable);

    @EntityGraph(attributePaths = "prestador")
    Optional<Servico> findWithPrestadorById(Long id);
}
