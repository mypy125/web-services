package com.mygitgor.seller_service.shared.valueobject.page;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Pageable {
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private SortDirection direction = SortDirection.DESC;

    public enum SortDirection {
        ASC,
        DESC;

        public boolean isAscending() {
            return this == ASC;
        }

        public boolean isDescending() {
            return this == DESC;
        }
    }

    public int getOffset() {
        return page * size;
    }

    public boolean isDescending() {
        return direction == SortDirection.DESC;
    }

    public boolean isAscending() {
        return direction == SortDirection.ASC;
    }

    public static Pageable of(int page, int size) {
        return Pageable.builder()
                .page(page)
                .size(size)
                .build();
    }

    public static Pageable of(int page, int size, String sortBy) {
        return Pageable.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .build();
    }

    public static Pageable of(int page, int size, String sortBy, SortDirection direction) {
        return Pageable.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .direction(direction)
                .build();
    }

    @Override
    public String toString() {
        return String.format("Pageable{page=%d, size=%d, sortBy='%s', direction=%s}",
                page, size, sortBy, direction);
    }
}
