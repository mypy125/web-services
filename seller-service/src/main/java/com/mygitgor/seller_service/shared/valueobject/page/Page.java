package com.mygitgor.seller_service.shared.valueobject.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private String sortBy;
    private String sortDirection;

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
        if (content == null) {
            content = Collections.emptyList();
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean first = pageNumber == 0;
        boolean last = pageNumber >= totalPages - 1 || totalPages == 0;
        int numberOfElements = content.size();
        boolean empty = content.isEmpty();

        return Page.<T>builder()
                .content(content)
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

    public static <T> Page<T> of(List<T> content, int pageNumber, int pageSize, long totalElements,
                                 String sortBy, String sortDirection) {
        Page<T> page = of(content, pageNumber, pageSize, totalElements);
        page.setSortBy(sortBy);
        page.setSortDirection(sortDirection);
        return page;
    }

    public static <T, U> Page<U> of(Page<T> sourcePage, Function<? super T, ? extends U> mapper) {
        List<U> mappedContent = sourcePage.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return Page.<U>builder()
                .content(mappedContent)
                .pageNumber(sourcePage.getPageNumber())
                .pageSize(sourcePage.getPageSize())
                .totalElements(sourcePage.getTotalElements())
                .totalPages(sourcePage.getTotalPages())
                .last(sourcePage.isLast())
                .first(sourcePage.isFirst())
                .numberOfElements(mappedContent.size())
                .empty(mappedContent.isEmpty())
                .sortBy(sourcePage.getSortBy())
                .sortDirection(sourcePage.getSortDirection())
                .build();
    }

    public boolean hasNext() {
        return !last && pageNumber < totalPages - 1;
    }

    public boolean hasPrevious() {
        return !first && pageNumber > 0;
    }

    public int nextPageNumber() {
        return hasNext() ? pageNumber + 1 : pageNumber;
    }

    public int previousPageNumber() {
        return hasPrevious() ? pageNumber - 1 : pageNumber;
    }

    public long getOffset() {
        return (long) pageNumber * pageSize;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }

    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }

    public <U> Page<U> map(Function<? super T, ? extends U> mapper) {
        List<U> mappedContent = content.stream()
                .map(mapper)
                .collect(Collectors.toList());

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
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }

    public <U> Page<U> transform(Function<? super T, ? extends U> mapper) {
        List<U> mappedContent = content.stream()
                .map(mapper)
                .collect(Collectors.toList());

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
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }

    public Page<T> filter(java.util.function.Predicate<? super T> predicate) {
        List<T> filteredContent = content.stream()
                .filter(predicate)
                .collect(Collectors.toList());

        return Page.<T>builder()
                .content(filteredContent)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements((long) filteredContent.size())
                .totalPages((int) Math.ceil((double) filteredContent.size() / pageSize))
                .last(true)
                .first(pageNumber == 0)
                .numberOfElements(filteredContent.size())
                .empty(filteredContent.isEmpty())
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }

    public Page<T> concat(Page<T> other) {
        if (other == null || other.isEmpty()) {
            return this;
        }

        List<T> combinedContent = new ArrayList<>(this.content);
        combinedContent.addAll(other.getContent());

        long total = this.totalElements + other.getTotalElements();

        return Page.<T>builder()
                .content(combinedContent)
                .pageNumber(this.pageNumber)
                .pageSize(this.pageSize)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / this.pageSize))
                .last(other.isLast())
                .first(this.isFirst())
                .numberOfElements(combinedContent.size())
                .empty(combinedContent.isEmpty())
                .sortBy(this.sortBy)
                .sortDirection(this.sortDirection)
                .build();
    }

    @Override
    public String toString() {
        return String.format(
                "Page{pageNumber=%d, pageSize=%d, totalElements=%d, totalPages=%d, " +
                        "numberOfElements=%d, empty=%s, last=%s, first=%s}",
                pageNumber, pageSize, totalElements, totalPages,
                numberOfElements, empty, last, first
        );
    }
}