package com.mygitgor.user_service.infrastructure.shared.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {

    @Builder.Default
    private List<T> content = new ArrayList<>();
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private int numberOfElements;
    private boolean empty;

    public static <T> Page<T> empty() {
        return Page.<T>builder()
                .content(Collections.emptyList())
                .pageNumber(0)
                .pageSize(0)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .first(true)
                .numberOfElements(0)
                .empty(true)
                .build();
    }

    public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean first = pageNumber == 0;
        boolean last = pageNumber >= totalPages - 1;
        int numberOfElements = content != null ? content.size() : 0;
        boolean empty = content == null || content.isEmpty();

        return Page.<T>builder()
                .content(content != null ? content : Collections.emptyList())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .first(first)
                .numberOfElements(numberOfElements)
                .empty(empty)
                .build();
    }

    public boolean hasNext() {
        return !last;
    }

    public boolean hasPrevious() {
        return !first;
    }

    public int nextPageNumber() {
        return hasNext() ? pageNumber + 1 : pageNumber;
    }

    public int previousPageNumber() {
        return hasPrevious() ? pageNumber - 1 : pageNumber;
    }

    public <U> Page<U> map(java.util.function.Function<T, U> mapper) {
        List<U> mappedContent = content.stream()
                .map(mapper)
                .collect(java.util.stream.Collectors.toList());

        return Page.<U>builder()
                .content(mappedContent)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .first(first)
                .numberOfElements(mappedContent.size())
                .empty(mappedContent.isEmpty())
                .build();
    }

    public long getOffset() {
        return (long) pageNumber * pageSize;
    }

    @Override
    public String toString() {
        return String.format("Page{pageNumber=%d, pageSize=%d, totalElements=%d, totalPages=%d, empty=%s}",
                pageNumber, pageSize, totalElements, totalPages, empty);
    }
}
