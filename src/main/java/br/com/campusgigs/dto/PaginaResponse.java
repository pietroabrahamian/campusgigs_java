package br.com.campusgigs.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envelope de paginacao proprio - evita serializar o PageImpl do Spring Data,
 * cujo formato JSON nao e garantido entre versoes.
 */
public record PaginaResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas,
        boolean ultima
) {

    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> conversor) {
        return new PaginaResponse<>(
                page.getContent().stream().map(conversor).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
