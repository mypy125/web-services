package com.mygitgor.seller_service.infrastructure.client;

import com.mygitgor.seller_service.application.dto.external.CategorySummaryDto;
import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.domain.model.Product;
import com.mygitgor.seller_service.domain.model.status.ProductStatus;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.port.outgoing.ProductPort;
import com.mygitgor.seller_service.infrastructure.client.exception.ProductServiceException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceTimeoutException;
import com.mygitgor.seller_service.infrastructure.client.exception.ServiceUnavailableException;
import com.mygitgor.seller_service.infrastructure.client.fallback.ProductServiceFallback;
import com.mygitgor.seller_service.infrastructure.client.intercepter.ServiceClientInterceptor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient implements ProductPort {
    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final ProductServiceFallback fallback;

    @Value("${product.service.url:http://localhost:8088/api/v1/products}")
    private String baseUrl;

    @Value("${product.service.timeout:5000}")
    private int timeout;

    @Value("${product.service.retry.attempts:3}")
    private int retryAttempts;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(clientInterceptor.logRequest())
                .filter(clientInterceptor.logResponse())
                .filter(clientInterceptor.handleErrors())
                .build();
    }

    private Mono<Throwable> handleClientErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Client error during {} for {}: Status={}", operation, identifier, response.statusCode());

        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> switch (response.statusCode().value()) {
                    case 404 -> ProductServiceException.notFound(identifier);
                    case 400 -> ProductServiceException.invalidRequest(identifier, errorBody);
                    case 403 -> ProductServiceException.accessDenied(identifier);
                    default -> new ProductServiceException(operation, response.statusCode().value(), "Client error: " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Product Service", operation));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductDetailsFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Mono<ProductDetailsDto> getProductDetails(ProductId productId) {
        log.debug("Getting product details for: {}", productId);
        return webClient.get()
                .uri("/{productId}/details", productId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_PRODUCT_DETAILS", productId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_PRODUCT_DETAILS", productId.toString()))
                .bodyToMono(ProductDetailsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", "GET_PRODUCT_DETAILS", (long) timeout, e)) : Mono.error(e))
                .retryWhen(reactor.util.retry.Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(t -> t instanceof ServiceUnavailableException))
                .doOnSuccess(details -> log.debug("Product details fetched: {}", productId));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Mono<Product> getProductById(ProductId productId) {
        log.debug("Getting domain product by ID: {}", productId);
        return webClient.get()
                .uri("/{productId}", productId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_PRODUCT_BY_ID", productId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_PRODUCT_BY_ID", productId.toString()))
                .bodyToMono(Product.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", "GET_PRODUCT_BY_ID", (long) timeout, e)) : Mono.error(e));
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getProductsBySellerId(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products", sellerId.toString(), null, null, page, size, "GET_SELLER_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getActiveProductsBySellerId(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/active", sellerId.toString(), null, null, page, size, "GET_ACTIVE_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getProductsBySellerIdSummary(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/summary", sellerId.toString(), null, null, page, size, "GET_PRODUCTS_SUMMARY");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getProductsByStatus(SellerId sellerId, ProductStatus status, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/status", sellerId.toString(), "status", status.name(), page, size, "GET_PRODUCTS_BY_STATUS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getProductsByCategory(SellerId sellerId, String categoryId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/category", sellerId.toString(), "category", categoryId, page, size, "GET_PRODUCTS_BY_CATEGORY");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getLowStockProductsBySellerId(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/low-stock", sellerId.toString(), null, null, page, size, "GET_LOW_STOCK_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getOnSaleProductsBySellerId(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/on-sale", sellerId.toString(), null, null, page, size, "GET_ON_SALE_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    @Retry(name = "productService")
    @TimeLimiter(name = "productService")
    public Flux<ProductSummaryDto> getProductsByPriceRange(SellerId sellerId, Double minPrice, Double maxPrice, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/products/price-range")
                        .queryParam("minPrice", minPrice)
                        .queryParam("maxPrice", maxPrice)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_BY_PRICE_RANGE", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_BY_PRICE_RANGE", sellerId.toString()))
                .bodyToFlux(ProductSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", "GET_BY_PRICE_RANGE", (long) timeout, e)) : Flux.error(e));
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> getTopProductsBySellerId(SellerId sellerId, int limit) {
        return getFilteredFlux("/sellers/{sellerId}/products/top", sellerId.toString(), "limit", String.valueOf(limit), 0, 0, "GET_TOP_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> getNewProductsBySellerId(SellerId sellerId, int limit) {
        return getFilteredFlux("/sellers/{sellerId}/products/new", sellerId.toString(), "limit", String.valueOf(limit), 0, 0, "GET_NEW_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> getTopRatedProductsBySellerId(SellerId sellerId, int limit) {
        return getFilteredFlux("/sellers/{sellerId}/products/top-rated", sellerId.toString(), "limit", String.valueOf(limit), 0, 0, "GET_TOP_RATED");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> getFeaturedProductsBySellerId(SellerId sellerId, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/featured", sellerId.toString(), null, null, page, size, "GET_FEATURED_PRODUCTS");
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> searchProductsBySellerId(SellerId sellerId, String searchTerm, int page, int size) {
        return getFilteredFlux("/sellers/{sellerId}/products/search", sellerId.toString(), "query", searchTerm, page, size, "SEARCH_PRODUCTS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> searchProductsWithFilters(SellerId sellerId, String searchTerm, String categoryId, Double minPrice, Double maxPrice, ProductStatus status, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/products/search/filter")
                        .queryParam("query", searchTerm)
                        .queryParam("category", categoryId)
                        .queryParam("minPrice", minPrice)
                        .queryParam("maxPrice", maxPrice)
                        .queryParam("status", status != null ? status.name() : null)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "SEARCH_PRODUCTS_FILTERS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "SEARCH_PRODUCTS_FILTERS", sellerId.toString()))
                .bodyToFlux(ProductSummaryDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", "SEARCH_PRODUCTS_FILTERS", (long) timeout, e)) : Flux.error(e));
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getStatisticsFallback")
    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId) {
        return webClient.get()
                .uri("/sellers/{sellerId}/statistics", sellerId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_PRODUCT_STATS", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_PRODUCT_STATS", sellerId.toString()))
                .bodyToMono(ProductStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", "GET_PRODUCT_STATS", (long) timeout, e)) : Mono.error(e));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getStatisticsWithDatesFallback")
    public Mono<ProductStatisticsDto> getProductStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sellers/{sellerId}/statistics/range")
                        .queryParam("start", startDate.toString())
                        .queryParam("end", endDate.toString())
                        .build(sellerId.toString()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_PRODUCT_STATS_DATES", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_PRODUCT_STATS_DATES", sellerId.toString()))
                .bodyToMono(ProductStatisticsDto.class)
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", "GET_PRODUCT_STATS_DATES", (long) timeout, e)) : Mono.error(e));
    }

    @Override
    public Mono<Long> countProductsBySellerId(SellerId s) {
        return getCount("/sellers/{sellerId}/count", s.toString(), "COUNT_PRODUCTS");
    }

    @Override
    public Mono<Long> countActiveProductsBySellerId(SellerId s) {
        return getCount("/sellers/{sellerId}/count/active", s.toString(), "COUNT_ACTIVE");
    }

    @Override
    public Mono<Long> countProductsByStatus(SellerId s, ProductStatus st) {
        return getCount("/sellers/{sellerId}/count/status?status=" + st.name(), s.toString(), "COUNT_STATUS");
    }

    @Override
    public Mono<Long> countProductsByCategory(SellerId s, String c) {
        return getCount("/sellers/{sellerId}/count/category?category=" + c, s.toString(), "COUNT_CATEGORY");
    }

    @Override
    public Mono<Long> countLowStockProductsBySellerId(SellerId s) {
        return getCount("/sellers/{sellerId}/count/low-stock", s.toString(), "COUNT_LOW_STOCK");
    }

    @Override
    public Mono<Long> countOnSaleProductsBySellerId(SellerId s) {
        return getCount("/sellers/{sellerId}/count/on-sale", s.toString(), "COUNT_ON_SALE");
    }

    @Override
    public Mono<Boolean> hasProducts(SellerId s) {
        return countProductsBySellerId(s).map(count -> count > 0).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> existsProduct(ProductId p) {
        return getCount("/{productId}/exists", p.toString(), "EXISTS_PRODUCT").map(c -> c > 0).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> isProductActive(ProductId p) {
        return getCount("/{productId}/is-active", p.toString(), "IS_ACTIVE").map(c -> c > 0).onErrorReturn(false);
    }

    @Override
    public Mono<Boolean> isProductInStock(ProductId p) {
        return getCount("/{productId}/in-stock", p.toString(), "IN_STOCK").map(c -> c > 0).onErrorReturn(false);
    }

    @Override
    public Mono<Integer> getAvailableQuantity(ProductId p) {
        return getCount("/{productId}/quantity", p.toString(), "GET_QUANTITY").map(Long::intValue).onErrorReturn(0);
    }

    @Override
    public Mono<Boolean> isProductBelongsToSeller(ProductId productId, SellerId sellerId) {
        return getProductDetails(productId)
                .map(product -> product.sellerId().equalsIgnoreCase(sellerId.toString()))
                .onErrorReturn(false);
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getMapStatisticsFallback")
    public Mono<Map<String, Long>> getCategoryStatistics(SellerId sellerId) {
        return getMapStats("/sellers/{sellerId}/statistics/categories", sellerId.toString(), "CAT_STATS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getMapStatisticsFallback")
    public Mono<Map<String, Long>> getStatusStatistics(SellerId sellerId) {
        return getMapStats("/sellers/{sellerId}/statistics/statuses", sellerId.toString(), "STATUS_STATS");
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "mutationFallback")
    public Mono<Product> createProduct(Product product) {
        return postMutation("", product, "CREATE_PRODUCT");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "mutationFallback")
    public Mono<Product> updateProduct(Product product) {
        return putMutation("/" + product.getId().toString(), product, "UPDATE_PRODUCT");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> deleteProduct(ProductId productId) {
        return deleteMutation("/{productId}", productId.toString(), "DELETE_PRODUCT");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> deleteProduct(ProductId productId, SellerId sellerId) {
        return deleteMutation("/" + productId + "?sellerId=" + sellerId, productId.toString(), "DELETE_PRODUCT_SECURE");
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> reserveProductStock(ProductId p, Integer q) {
        return postMutationVoid("/{productId}/reserve?quantity=" + q, p.toString(), "RESERVE_STOCK");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> releaseReservedStock(ProductId p, Integer q) {
        return postMutationVoid("/{productId}/release?quantity=" + q, p.toString(), "RELEASE_STOCK");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> sellProductStock(ProductId p, Integer q) {
        return postMutationVoid("/{productId}/sell?quantity=" + q, p.toString(), "SELL_STOCK");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> returnProductStock(ProductId p, Integer q) {
        return postMutationVoid("/{productId}/return?quantity=" + q, p.toString(), "RETURN_STOCK");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> updateProductStock(ProductId p, Integer q) {
        return putMutationVoid("/{productId}/stock?quantity=" + q, p.toString(), "UPDATE_STOCK");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> updateFeaturedStatus(ProductId p, boolean f) {
        return putMutationVoid("/{productId}/featured?featured=" + f, p.toString(), "UPDATE_FEATURED");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> updateProductRating(ProductId p, Double rating, Integer count) {
        return putMutationVoid("/" + p + "/rating?rating=" + rating + "&reviewCount=" + count, p.toString(), "UPDATE_RATING");
    }


    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "bulkMutationFallback")
    public Flux<Product> bulkCreateProducts(List<Product> products) {
        return postBulkMutation("/bulk", products, "BULK_CREATE");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "bulkMutationFallback")
    public Flux<Product> bulkUpdateStatus(List<ProductId> ids, ProductStatus status) {
        return putBulkMutation("/bulk/status?status=" + status.name(), ids, "BULK_STATUS");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "bulkMutationFallback")
    public Flux<Product> bulkUpdatePrices(Map<ProductId, Double> updates) {
        return putBulkMutation("/bulk/prices", updates, "BULK_PRICES");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "bulkMutationFallback")
    public Flux<Product> bulkUpdateQuantities(Map<ProductId, Integer> updates) {
        return putBulkMutation("/bulk/quantities", updates, "BULK_QUANTITIES");
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "voidMutationFallback")
    public Mono<Void> bulkDeleteProducts(List<ProductId> productIds) {
        return webClient.post().uri("/bulk/delete").bodyValue(productIds).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "BULK_DELETE", "batch"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "BULK_DELETE", "batch"))
                .bodyToMono(Void.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", "BULK_DELETE", (long) timeout, e)) : Mono.error(e));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductsBySellerIdFallback")
    public Flux<ProductSummaryDto> getProductsByCategories(SellerId sellerId, List<String> categoryIds, int page, int size) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/sellers/{sellerId}/products/categories").queryParam("page", page).queryParam("size", size).build(sellerId.toString()))
                .bodyValue(categoryIds).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_BY_CATEGORIES", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_BY_CATEGORIES", sellerId.toString()))
                .bodyToFlux(ProductSummaryDto.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", "GET_BY_CATEGORIES", (long) timeout, e)) : Flux.error(e));
    }

    @Override
    @CircuitBreaker(name = "productService", fallbackMethod = "getCategoriesFallback")
    public Flux<CategorySummaryDto> getCategoriesBySellerId(SellerId sellerId) {
        return webClient.get().uri("/sellers/{sellerId}/categories", sellerId.toString()).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, "GET_CATEGORIES", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, "GET_CATEGORIES", sellerId.toString()))
                .bodyToFlux(CategorySummaryDto.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", "GET_CATEGORIES", (long) timeout, e)) : Flux.error(e));
    }


    private Flux<ProductSummaryDto> getFilteredFlux(String path, String uriVar, String pName, String pVal, int page, int size, String operation) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    if (pName != null && pVal != null) builder.queryParam(pName, pVal);
                    if (page >= 0) builder.queryParam("page", page).queryParam("size", size);
                    return builder.build(uriVar);
                })
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, uriVar))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, uriVar))
                .bodyToFlux(ProductSummaryDto.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Flux.error(e));
    }

    private Mono<Long> getCount(String path, String uriVar, String operation) {
        return webClient.get().uri(path, uriVar).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, uriVar))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, uriVar))
                .bodyToMono(Long.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    @SuppressWarnings("unchecked")
    private Mono<Map<String, Long>> getMapStats(String path, String uriVar, String operation) {
        return webClient.get()
                .uri(path, uriVar)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, uriVar))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, uriVar))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Mono<Product> postMutation(String path, Object body, String operation) {
        return webClient.post().uri(path).bodyValue(body).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, "body"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, "body"))
                .bodyToMono(Product.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Mono<Product> putMutation(String path, Object body, String operation) {
        return webClient.put().uri(path).bodyValue(body).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, "body"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, "body"))
                .bodyToMono(Product.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Mono<Void> postMutationVoid(String path, String identifier, String operation) {
        return webClient.post().uri(path).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, identifier))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, identifier))
                .bodyToMono(Void.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Mono<Void> putMutationVoid(String path, String identifier, String operation) {
        return webClient.put().uri(path).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, identifier))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, identifier))
                .bodyToMono(Void.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Mono<Void> deleteMutation(String path, String identifier, String operation) {
        return webClient.delete().uri(path).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, identifier))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, identifier))
                .bodyToMono(Void.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Mono.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Mono.error(e));
    }

    private Flux<Product> postBulkMutation(String path, Object body, String operation) {
        return webClient.post().uri(path).bodyValue(body).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, "bulk"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, "bulk"))
                .bodyToFlux(Product.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Flux.error(e));
    }

    private Flux<Product> putBulkMutation(String path, Object body, String operation) {
        return webClient.put().uri(path).bodyValue(body).accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, r -> handleClientErrorResponse(r, operation, "bulk"))
                .onStatus(HttpStatusCode::is5xxServerError, r -> handleServerErrorResponse(r, operation, "bulk"))
                .bodyToFlux(Product.class).timeout(Duration.ofMillis(timeout))
                .onErrorResume(Throwable.class, e -> e instanceof java.util.concurrent.TimeoutException ?
                        Flux.error(new ServiceTimeoutException("Product Service", operation, (long) timeout, e)) : Flux.error(e));
    }

    private Mono<ProductDetailsDto> getProductDetailsFallback(ProductId productId, Throwable t) {
        log.warn("Fallback triggered for getProductDetails on product: {}. Reason: {}", productId, t.getMessage());
        return fallback.getProductDetails(productId);
    }

    private Mono<Product> getProductByIdFallback(ProductId productId, Throwable t) {
        log.warn("Fallback triggered for getProductById on product: {}. Reason: {}", productId, t.getMessage());
        return fallback.getProductById(productId);
    }

    private Flux<ProductSummaryDto> getProductsBySellerIdFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback triggered for fetching products list for seller: {}. Reason: {}", sellerId, t.getMessage());
        return fallback.getProductsBySellerId(sellerId);
    }

    private Mono<ProductStatisticsDto> getStatisticsFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback triggered for getProductStatistics for seller: {}. Reason: {}", sellerId, t.getMessage());
        return fallback.getProductStatistics(sellerId);
    }

    private Mono<ProductStatisticsDto> getStatisticsWithDatesFallback(SellerId sellerId, LocalDateTime s, LocalDateTime e, Throwable t) {
        log.warn("Fallback triggered for getProductStatistics with range for seller: {}. Reason: {}", sellerId, t.getMessage());
        return fallback.getProductStatistics(sellerId, s, e);
    }

    private Mono<Map<String, Long>> getMapStatisticsFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback triggered for map statistics for seller: {}. Reason: {}", sellerId, t.getMessage());
        return fallback.getMapStatistics(sellerId);
    }

    private Flux<CategorySummaryDto> getCategoriesFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback triggered for getCategories for seller: {}. Reason: {}", sellerId, t.getMessage());
        return fallback.getCategoriesBySellerId(sellerId);
    }

    private Mono<Product> mutationFallback(Product product, Throwable t) {
        log.error("Fallback: Direct mutation for product failed due to service outage. Reason: {}", t.getMessage());
        return Mono.error(ProductServiceException.unavailable("MUTATE_PRODUCT"));
    }

    private Flux<Product> bulkMutationFallback(Object body, Throwable t) {
        log.error("Fallback: Bulk transaction mutation failed. Reason: {}", t.getMessage());
        return Flux.error(ProductServiceException.unavailable("BULK_MUTATE"));
    }

    private Mono<Void> voidMutationFallback(ProductId p, Throwable t) {
        log.error("Fallback: Void mutation for product {} failed. Reason: {}", p, t.getMessage());
        return Mono.error(ProductServiceException.unavailable("VOID_MUTATE"));
    }

    private Mono<Void> voidMutationFallback(Object arg1, Object arg2, Throwable t) {
        log.error("Fallback: Complex void state update failed. Reason: {}", t.getMessage());
        return Mono.error(ProductServiceException.unavailable("VOID_MUTATE_COMPLEX"));
    }
}