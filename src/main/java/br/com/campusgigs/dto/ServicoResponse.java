package br.com.campusgigs.dto;

import br.com.campusgigs.domain.Servico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponse(
        Long id,
        String titulo,
        String descricao,
        String categoria,
        BigDecimal preco,
        String situacao,
        UsuarioResumo prestador,
        LocalDateTime criadoEm
) {

    public static ServicoResponse de(Servico servico) {
        return new ServicoResponse(
                servico.getId(),
                servico.getTitulo(),
                servico.getDescricao(),
                servico.getCategoria(),
                servico.getPreco(),
                servico.getSituacao().name(),
                UsuarioResumo.de(servico.getPrestador()),
                servico.getCriadoEm());
    }
}
