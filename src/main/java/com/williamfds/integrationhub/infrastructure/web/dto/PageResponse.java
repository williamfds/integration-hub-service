package com.williamfds.integrationhub.infrastructure.web.dto;

import com.williamfds.integrationhub.domain.repository.OrderRepository;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> items, int page, int size, long totalElements) {

    public static <S, T> PageResponse<T> of(OrderRepository.Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.items().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements()
        );
    }
}
