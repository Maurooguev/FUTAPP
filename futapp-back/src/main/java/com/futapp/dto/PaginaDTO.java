package com.futapp.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PaginaDTO<T>(List<T> content, int pagina, int cantidad, long totalElementos, int totalPaginas,
                           boolean primera, boolean ultima) {
    public static <T> PaginaDTO<T> desde(Page<T> page) {
        return new PaginaDTO<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
