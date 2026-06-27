package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductStatisticsDto;
import com.mygitgor.seller_service.application.dto.external.ProductSummaryDto;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
}
