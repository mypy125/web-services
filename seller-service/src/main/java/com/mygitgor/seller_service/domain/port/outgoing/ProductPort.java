package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.external.CategorySummaryDto;
import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.shared.valueobject.Product;
import com.mygitgor.seller_service.shared.valueobject.ProductStatus;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProductPort {
    Mono<ProductDetailsDto> getProductDetails(ProductId productId);
    Flux<ProductSummaryDto> getProductsBySellerId(SellerId sellerId, int page, int size);
    Flux<ProductSummaryDto> getActiveProductsBySellerId(SellerId sellerId, int page, int size);
    Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId);
    Mono<Long> countProductsBySellerId(SellerId sellerId);
    Mono<Long> countActiveProductsBySellerId(SellerId sellerId);
    Mono<Boolean> hasProducts(SellerId sellerId);
    Mono<Boolean> isProductBelongsToSeller(ProductId productId, SellerId sellerId);
    Flux<ProductSummaryDto> getTopProductsBySellerId(SellerId sellerId, int limit);
    Mono<Product> createProduct(Product product);
    Mono<Product> updateProduct(Product product);
    Mono<Product> getProductById(ProductId productId);
    Mono<Void> deleteProduct(ProductId productId);
    Mono<Void> deleteProduct(ProductId productId, SellerId sellerId);
    Flux<ProductSummaryDto> getProductsBySellerIdSummary(SellerId sellerId, int page, int size);
    Flux<ProductSummaryDto> getProductsByStatus(SellerId sellerId, ProductStatus status, int page, int size);
    Flux<ProductSummaryDto> getProductsByCategory(SellerId sellerId, String categoryId, int page, int size);
    Flux<ProductSummaryDto> getLowStockProductsBySellerId(SellerId sellerId, int page, int size);
    Flux<ProductSummaryDto> getOnSaleProductsBySellerId(SellerId sellerId, int page, int size);
    Flux<ProductSummaryDto> getNewProductsBySellerId(SellerId sellerId, int limit);
    Flux<ProductSummaryDto> getProductsByPriceRange(SellerId sellerId, Double minPrice, Double maxPrice, int page, int size);
    Mono<Boolean> existsProduct(ProductId productId);
    Mono<Boolean> isProductActive(ProductId productId);
    Mono<Boolean> isProductInStock(ProductId productId);
    Mono<Integer> getAvailableQuantity(ProductId productId);
    Mono<Long> countProductsByStatus(SellerId sellerId, ProductStatus status);
    Mono<Long> countProductsByCategory(SellerId sellerId, String categoryId);
    Mono<Long> countLowStockProductsBySellerId(SellerId sellerId);
    Mono<Long> countOnSaleProductsBySellerId(SellerId sellerId);
    Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate);
    Mono<Map<String, Long>> getCategoryStatistics(SellerId sellerId);
    Mono<Map<String, Long>> getStatusStatistics(SellerId sellerId);
    Flux<ProductSummaryDto> searchProductsBySellerId(SellerId sellerId, String searchTerm, int page, int size);
    Flux<ProductSummaryDto> searchProductsWithFilters(
            SellerId sellerId,
            String searchTerm,
            String categoryId,
            Double minPrice,
            Double maxPrice,
            ProductStatus status,
            int page,
            int size
    );
    Mono<Void> reserveProductStock(ProductId productId, Integer quantity);
    Mono<Void> releaseReservedStock(ProductId productId, Integer quantity);
    Mono<Void> sellProductStock(ProductId productId, Integer quantity);
    Mono<Void> returnProductStock(ProductId productId, Integer quantity);
    Mono<Void> updateProductStock(ProductId productId, Integer newQuantity);
    Flux<Product> bulkCreateProducts(List<Product> products);
    Flux<Product> bulkUpdateStatus(List<ProductId> productIds, ProductStatus status);
    Flux<Product> bulkUpdatePrices(Map<ProductId, Double> priceUpdates);
    Flux<Product> bulkUpdateQuantities(Map<ProductId, Integer> quantityUpdates);
    Mono<Void> bulkDeleteProducts(List<ProductId> productIds);
    Flux<ProductSummaryDto> getProductsByCategories(SellerId sellerId, List<String> categoryIds, int page, int size);
    Flux<CategorySummaryDto> getCategoriesBySellerId(SellerId sellerId);
    Mono<Void> updateProductRating(ProductId productId, Double rating, Integer reviewCount);
    Flux<ProductSummaryDto> getTopRatedProductsBySellerId(SellerId sellerId, int limit);
    Flux<ProductSummaryDto> getFeaturedProductsBySellerId(SellerId sellerId, int page, int size);
    Mono<Void> updateFeaturedStatus(ProductId productId, boolean featured);
}
