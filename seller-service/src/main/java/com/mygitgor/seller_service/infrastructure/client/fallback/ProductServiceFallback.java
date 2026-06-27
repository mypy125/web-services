package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ProductServiceFallback {

    public Mono<ProductDetailsDto> getProductDetails(ProductId productId) {
        log.warn("Fallback: Returning empty product details for: {}", productId);
        return Mono.empty();
    }

    public Mono<ProductDetailsDto> getProductDetailsWithDefault(ProductId productId) {
        log.warn("Fallback: Returning default product details for: {}", productId);
        return Mono.just(ProductDetailsDto.builder()
                .id(productId != null ? productId.toString() : "unknown")
                .name("Product Unavailable")
                .description("Product details currently unavailable")
                .category("Uncategorized")
                .price(0.0)
                .availableQuantity(0)
                .isActive(false)
                .inStock(false)
                .build());
    }

    public Flux<ProductSummaryDto> getProductsBySellerId(SellerId sellerId, int page, int size) {
        log.warn("Fallback: Returning empty products for seller: {}, page: {}, size: {}", sellerId, page, size);
        return Flux.empty();
    }

    public Flux<ProductSummaryDto> getProductsBySellerIdWithDefaults(SellerId sellerId, int page, int size) {
        log.warn("Fallback: Returning default products for seller: {}", sellerId);
        return Flux.just(ProductSummaryDto.builder()
                .id("default-product-1")
                .sellerId(sellerId != null ? sellerId.toString() : "unknown")
                .name("Product Unavailable")
                .category("Uncategorized")
                .price(0.0)
                .availableQuantity(0)
                .isActive(false)
                .inStock(false)
                .build());
    }

    public Flux<ProductSummaryDto> getActiveProductsBySellerId(SellerId sellerId, int page, int size) {
        log.warn("Fallback: Returning empty active products for seller: {}", sellerId);
        return Flux.empty();
    }

    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId) {
        log.warn("Fallback: Returning empty product statistics for seller: {}", sellerId);
        return Mono.empty();
    }

    public Mono<ProductStatisticsDto> getProductStatisticsWithDefaults(SellerId sellerId) {
        log.warn("Fallback: Returning default product statistics for seller: {}", sellerId);
        return Mono.just(ProductStatisticsDto.builder()
                .sellerId(sellerId != null ? sellerId.toString() : "unknown")
                .totalProducts(0)
                .activeProducts(0)
                .inactiveProducts(0)
                .outOfStockProducts(0)
                .totalRevenue(0.0)
                .averagePrice(0.0)
                .totalProductViews(0L)
                .totalProductSales(0L)
                .calculatedAt(java.time.LocalDateTime.now())
                .build());
    }

    public Mono<Long> countProductsBySellerId(SellerId sellerId) {
        log.warn("Fallback: Returning 0 products count for seller: {}", sellerId);
        return Mono.just(0L);
    }

    public Mono<Long> countActiveProductsBySellerId(SellerId sellerId) {
        log.warn("Fallback: Returning 0 active products count for seller: {}", sellerId);
        return Mono.just(0L);
    }

    public Mono<Boolean> hasProducts(SellerId sellerId) {
        log.warn("Fallback: Returning false for hasProducts for seller: {}", sellerId);
        return Mono.just(false);
    }

    public Mono<Boolean> isProductBelongsToSeller(ProductId productId, SellerId sellerId) {
        log.warn("Fallback: Returning false for product belongs to seller: {}", productId);
        return Mono.just(false);
    }

    public Flux<ProductSummaryDto> getTopProductsBySellerId(SellerId sellerId, int limit) {
        log.warn("Fallback: Returning empty top products for seller: {}, limit: {}", sellerId, limit);
        return Flux.empty();
    }

    public Flux<ProductSummaryDto> getTopProductsBySellerIdWithDefaults(SellerId sellerId, int limit) {
        log.warn("Fallback: Returning default top products for seller: {}", sellerId);
        return Flux.just(ProductSummaryDto.builder()
                .id("default-top-product")
                .sellerId(sellerId != null ? sellerId.toString() : "unknown")
                .name("Product Unavailable")
                .category("Uncategorized")
                .price(0.0)
                .totalQuantitySold(0)
                .totalRevenue(0.0)
                .averageRating(0.0)
                .build());
    }

    public Mono<Boolean> isValidCategory(String category) {
        log.warn("Fallback: Returning true for isValidCategory: {}", category);
        return Mono.just(true);
    }

    public Flux<String> getCategories() {
        log.warn("Fallback: Returning empty categories list");
        return Flux.empty();
    }

    public Flux<String> getCategoriesWithDefaults() {
        log.warn("Fallback: Returning default categories");
        return Flux.just("Uncategorized", "General");
    }

    public Flux<ProductSummaryDto> getProductsByIds(java.util.List<String> productIds) {
        log.warn("Fallback: Returning empty products for IDs: {}", productIds);
        return Flux.empty();
    }

    public Flux<ProductDetailsDto> getProductDetailsByIds(java.util.List<String> productIds) {
        log.warn("Fallback: Returning empty product details for IDs: {}", productIds);
        return Flux.empty();
    }

    public Flux<ProductSummaryDto> searchProducts(String searchTerm, int page, int size) {
        log.warn("Fallback: Returning empty products for search term: {}", searchTerm);
        return Flux.empty();
    }

    public Flux<ProductSummaryDto> searchProductsBySellerId(SellerId sellerId, String searchTerm, int page, int size) {
        log.warn("Fallback: Returning empty products for seller: {}, search term: {}", sellerId, searchTerm);
        return Flux.empty();
    }

    public Mono<Boolean> checkStockAvailability(ProductId productId, int quantity) {
        log.warn("Fallback: Returning false for stock availability for product: {}, quantity: {}", productId, quantity);
        return Mono.just(false);
    }

    public Mono<Integer> getAvailableQuantity(ProductId productId) {
        log.warn("Fallback: Returning 0 available quantity for product: {}", productId);
        return Mono.just(0);
    }
}
