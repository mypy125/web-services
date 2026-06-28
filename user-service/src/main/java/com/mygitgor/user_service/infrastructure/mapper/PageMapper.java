package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.shared.valueobject.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageMapper {

    public <T, U> Page<U> map(Page<T> sourcePage, Function<T, U> mapper) {
        if (sourcePage == null) {
            return Page.empty();
        }

        if (sourcePage.getContent() == null || sourcePage.getContent().isEmpty()) {
            return Page.of(
                    Collections.emptyList(),
                    sourcePage.getPageNumber(),
                    sourcePage.getPageSize(),
                    sourcePage.getTotalElements()
            );
        }

        List<U> mappedContent = sourcePage.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return Page.of(
                mappedContent,
                sourcePage.getPageNumber(),
                sourcePage.getPageSize(),
                sourcePage.getTotalElements()
        );
    }
}
