package com.mygitgor.seller_service.application.dto.external;

import lombok.Builder;

@Builder
public record CategorySummaryDto(
        String categoryId,
        String categoryName,
        Long productCount,
        Double totalRevenue,
        Double averagePrice
) {}
