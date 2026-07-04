package com.mygitgor.seller_service.domain.model.status;

import lombok.Getter;
import java.util.Arrays;
import java.util.Set;

@Getter
public enum ProductStatus {
    DRAFT("Draft", "Product Draft"),
    PENDING_APPROVAL("Pending Approval", "Product Awaiting approval"),
    PUBLISHED("Published", "Product Published"),
    IN_REVIEW("In Review", "Product Under review"),
    FLAGGED("Flagged", "Product Marked"),
    ARCHIVED("Archived", "Product In the archive"),
    DELETED("Deleted", "Product Removed");

    private final String displayName;
    private final String description;

    private static final Set<ProductStatus> ACTIVE_STATUSES = Set.of(PUBLISHED, PENDING_APPROVAL, IN_REVIEW);

    ProductStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isActive() {
        return ACTIVE_STATUSES.contains(this);
    }

    public static ProductStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            return DRAFT;
        }
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(status.trim()))
                .findFirst()
                .orElse(DRAFT);
    }
}