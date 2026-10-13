package com.example.common.web;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Common format for paged lists, so the frontend reads every list the same way.
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
