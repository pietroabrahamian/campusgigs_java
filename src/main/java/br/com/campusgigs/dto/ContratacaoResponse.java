package br.com.campusgigs.dto;

import br.com.campusgigs.domain.Contratacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContratacaoResponse(
        Long id,
        Long servicoId,
        String tituloServico,
        String situacao,
        BigDecimal precoAcordado,
        UsuarioResumo prestador,
        UsuarioResumo contratante,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static ContratacaoResponse de(Contratacao contratacao) {
        return new ContratacaoResponse(
                contratacao.getId(),
                contratacao.getServico().getId(),
                contratacao.getServico().getTitulo(),
                contratacao.getSituacao().name(),
                contratacao.getPrecoAcordado(),
                UsuarioResumo.de(contratacao.getServico().getPrestador()),
                UsuarioResumo.de(contratacao.getContratante()),
                contratacao.getCriadoEm(),
                contratacao.getAtualizadoEm());
    }
}
