package com.mygitgor.seller_service.infrastructure.client.fallback;

import com.mygitgor.seller_service.application.dto.external.CategorySummaryDto;
import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.domain.model.Product;
import com.mygitgor.seller_service.infrastructure.client.exception.ProductServiceException;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class ProductServiceFallback {

    public Mono<ProductDetailsDto> getProductDetails(ProductId productId) {
        log.warn("ProductService is down. Returning safe placeholder for details of product: {}", productId);
        return Mono.just(ProductDetailsDto.builder()
                .id(productId.toString())
                .sellerId("UNKNOWN")
                .name("Temporary Unavailable Product")
                .description("Product details are currently not available.")
                .category("N/A")
                .subCategory("N/A")
                .brand("N/A")
                .price(0.0)
                .compareAtPrice(0.0)
                .costPerItem(0.0)
                .profitMargin(0.0)
                .currency(null)
                .availableQuantity(0)
                .totalQuantitySold(0)
                .totalRevenue(0.0)
                .averageRating(0.0)
                .totalReviews(0)
                .isActive(false)
                .inStock(false)
                .isFeatured(false)
                .sku("N/A")
                .barcode("N/A")
                .weight(0.0)
                .dimensions("N/A")
                .mainImageUrl(null)
                .imageUrls(Collections.emptyList())
                .videoUrl(null)
                .tags(Collections.emptyList())
                .specifications(Collections.emptyMap())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .publishedAt(null)
                .shippingInfo("N/A")
                .returnPolicy("N/A")
                .warranty("N/A")
                .build());
    }

    public Mono<Product> getProductById(ProductId productId) {
        log.warn("ProductService is down. Breaking flow with 503 for domain root entity fetch: {}", productId);
        return Mono.error(ProductServiceException.unavailable("GET_PRODUCT_BY_ID"));
    }

    public Flux<ProductSummaryDto> getProductsBySellerId(SellerId sellerId) {
        log.warn("ProductService is down. Dropping out into an empty stream for seller list requests: {}", sellerId);
        return Flux.empty();
    }

    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId) {
        log.warn("ProductService is down. Instantiating baseline clean-zeros statistics blueprint for seller: {}", sellerId);
        return Mono.just(buildZeroedStatistics(sellerId.toString(), null, null));
    }

    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        log.warn("ProductService is down. Instantiating baseline clean-zeros range statistics blueprint for seller: {}", sellerId);
        return Mono.just(buildZeroedStatistics(sellerId.toString(), startDate, endDate));
    }

    public Mono<Map<String, Long>> getMapStatistics(SellerId sellerId) {
        log.warn("ProductService is down. Evacuating map metrics context to an empty structure map for: {}", sellerId);
        return Mono.just(Collections.emptyMap());
    }

    public Flux<CategorySummaryDto> getCategoriesBySellerId(SellerId sellerId) {
        log.warn("ProductService is down. Returning empty stream backoff for categories overview context: {}", sellerId);
        return Flux.empty();
    }

    // Внутренний хелпер для сборки чистой статистики без null-pointer рисков по типам данных
    private ProductStatisticsDto buildZeroedStatistics(String sellerId, LocalDateTime start, LocalDateTime end) {
        return ProductStatisticsDto.builder()
                .sellerId(sellerId)
                .totalProducts(0)
                .activeProducts(0)
                .inactiveProducts(0)
                .outOfStockProducts(0)
                .featuredProducts(0)
                .totalProductViews(0L)
                .totalProductSales(0L)
                .totalRevenue(0.0)
                .averagePrice(0.0)
                .minimumPrice(0.0)
                .maximumPrice(0.0)
                .mostSoldCategory("None")
                .mostSoldCategoryCount(0)
                .categoryDistribution(Collections.emptyMap())
                .topCategories(Collections.emptyList())
                .bestSellingProductId(null)
                .bestSellingProductName("N/A")
                .bestSellingProductSales(0)
                .highestRatedProductId(null)
                .highestRatedProductName("N/A")
                .highestRating(0.0)
                .mostViewedProductId(null)
                .mostViewedProductName("N/A")
                .mostViewedCount(0L)
                .totalInventoryValue(0.0)
                .averageInventoryValue(0.0)
                .lowStockProducts(0)
                .restockNeededProducts(0)
                .inventoryTurnRatio(0.0)
                .averageRatingOverall(0.0)
                .productsWithNoReviews(0)
                .productsWithGoodReviews(0)
                .productsWithPoorReviews(0)
                .newProductsThisMonth(0)
                .productsSoldThisMonth(0)
                .revenueThisMonth(0.0)
                .productsAddedLast7Days(0)
                .productViewsLast7Days(0L)
                .calculatedAt(LocalDateTime.now())
                .periodStart(start)
                .periodEnd(end)
                .build();
    }
}
