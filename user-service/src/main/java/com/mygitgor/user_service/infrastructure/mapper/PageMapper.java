package com.mygitgor.user_service.infrastructure.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PageMapper {

    public <T, U> Page<U> map(Page<T> sourcePage, Function<T, U> mapper) {
        if (sourcePage == null) {
            return Page.empty();
        }

        List<U> content = sourcePage.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageImpl<>(
                content,
                PageRequest.of(sourcePage.getNumber(), sourcePage.getSize()),
                sourcePage.getTotalElements()
        );
    }
}
