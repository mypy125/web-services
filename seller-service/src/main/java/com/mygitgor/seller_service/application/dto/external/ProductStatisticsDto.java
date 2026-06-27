package com.mygitgor.seller_service.application.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Schema(description = "Product statistics DTO represented as an immutable record")
public record ProductStatisticsDto(
        @Schema(description = "Seller ID", example = "sel-123e4567-e89b-12d3-a456-426614174000")
        String sellerId,

        @Schema(description = "Total products", example = "150")
        Integer totalProducts,

        @Schema(description = "Active products", example = "120")
        Integer activeProducts,

        @Schema(description = "Inactive products", example = "30")
        Integer inactiveProducts,

        @Schema(description = "Out of stock products", example = "15")
        Integer outOfStockProducts,

        @Schema(description = "Featured products", example = "25")
        Integer featuredProducts,

        @Schema(description = "Total product views", example = "15000")
        Long totalProductViews,

        @Schema(description = "Total product sales", example = "15000")
        Long totalProductSales,

        @Schema(description = "Total revenue", example = "1499998.50")
        Double totalRevenue,

        @Schema(description = "Average price", example = "99.99")
        Double averagePrice,

        @Schema(description = "Minimum price", example = "9.99")
        Double minimumPrice,

        @Schema(description = "Maximum price", example = "1999.99")
        Double maximumPrice,

        @Schema(description = "Most sold category", example = "Electronics")
        String mostSoldCategory,

        @Schema(description = "Most sold category count", example = "500")
        Integer mostSoldCategoryCount,

        @Schema(description = "Category distribution", example = "{\"Electronics\": 50, \"Books\": 30}")
        Map<String, Integer> categoryDistribution,

        @Schema(description = "Top 5 categories")
        List<CategorySalesDto> topCategories,

        @Schema(description = "Best selling product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String bestSellingProductId,

        @Schema(description = "Best selling product name", example = "iPhone 15 Pro")
        String bestSellingProductName,

        @Schema(description = "Best selling product sales", example = "500")
        Integer bestSellingProductSales,

        @Schema(description = "Highest rated product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String highestRatedProductId,

        @Schema(description = "Highest rated product name", example = "iPhone 15 Pro")
        String highestRatedProductName,

        @Schema(description = "Highest rating", example = "4.9")
        Double highestRating,

        @Schema(description = "Most viewed product ID", example = "prd-123e4567-e89b-12d3-a456-426614174000")
        String mostViewedProductId,

        @Schema(description = "Most viewed product name", example = "iPhone 15 Pro")
        String mostViewedProductName,

        @Schema(description = "Most viewed count", example = "5000")
        Long mostViewedCount,

        @Schema(description = "Total inventory value", example = "75000.00")
        Double totalInventoryValue,

        @Schema(description = "Average inventory value per product", example = "500.00")
        Double averageInventoryValue,

        @Schema(description = "Products with low stock", example = "10")
        Integer lowStockProducts,

        @Schema(description = "Products needing restock", example = "5")
        Integer restockNeededProducts,

        @Schema(description = "Inventory turn ratio", example = "2.5")
        Double inventoryTurnRatio,

        @Schema(description = "Average rating overall", example = "4.2")
        Double averageRatingOverall,

        @Schema(description = "Products with no reviews", example = "20")
        Integer productsWithNoReviews,

        @Schema(description = "Products with good reviews (>= 4 stars)", example = "80")
        Integer productsWithGoodReviews,

        @Schema(description = "Products with poor reviews (< 3 stars)", example = "10")
        Integer productsWithPoorReviews,

        @Schema(description = "New products this month", example = "15")
        Integer newProductsThisMonth,

        @Schema(description = "Products sold this month", example = "45")
        Integer productsSoldThisMonth,

        @Schema(description = "Revenue this month", example = "15000.00")
        Double revenueThisMonth,

        @Schema(description = "Product additions last 7 days", example = "5")
        Integer productsAddedLast7Days,

        @Schema(description = "Product views last 7 days", example = "3500")
        Long productViewsLast7Days,

        @Schema(description = "Statistics calculated at")
        LocalDateTime calculatedAt,

        @Schema(description = "Period start")
        LocalDateTime periodStart,

        @Schema(description = "Period end")
        LocalDateTime periodEnd
) {

    @Builder
    @Schema(description = "Category sales DTO represented as an immutable record")
    public record CategorySalesDto(
            @Schema(description = "Category name", example = "Electronics")
            String category,

            @Schema(description = "Sales count", example = "500")
            Integer salesCount,

            @Schema(description = "Revenue", example = "50000.00")
            Double revenue,

            @Schema(description = "Percentage of total", example = "33.3")
            Double percentage
    ) {}
}