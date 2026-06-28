package com.mygitgor.seller_service.application.service;

import com.mygitgor.seller_service.application.dto.request.CreateProductRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateProductRequest;
import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.port.outgoing.ProductPort;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.infrastructure.kafka.producer.SellerEventProducer;
import com.mygitgor.seller_service.infrastructure.mapper.ProductMapper;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.valueobject.Product;
import com.mygitgor.seller_service.shared.valueobject.ProductStatus;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

import reactor.core.scheduler.Schedulers;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerProductService {
    private final ProductPort productPort;
    private final SellerRepositoryPort sellerRepository;
    private final SellerEventProducer eventProducer;
    private final ProductMapper productMapper;

    private static final int MAX_PRODUCTS_PER_SELLER = 1000;
    private static final int MAX_BATCH_SIZE = 100;

    @Transactional
    public Mono<ProductResponse> createProduct(SellerId sellerId, CreateProductRequest request) {
        log.info("Creating product for seller: {}", sellerId);

        return validateSellerCanAddProducts(sellerId)
                .then(validateProductLimit(sellerId))
                .then(Mono.defer(() -> {
                    Product product = productMapper.toDomain(request, sellerId);
                    return productPort.createProduct(product);
                }))
                .map(productMapper::toResponse)
                .flatMap(response -> eventProducer.sendProductCreatedEvent(sellerId, response)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume((Throwable err) -> {
                            log.error("Failed to send ProductCreatedEvent for product: {}", response.id(), err);
                            return Mono.empty();
                        })
                        .then(Mono.just(response))
                )
                .doOnSuccess(product -> log.info("Product created successfully for seller: {}, product: {}", sellerId, product.id()))
                .doOnError(error -> log.error("Failed to create product for seller: {}", sellerId, error));
    }

    @Transactional
    public Flux<ProductResponse> createProducts(SellerId sellerId, List<CreateProductRequest> requests) {
        log.info("Creating {} products for seller: {}", requests.size(), sellerId);

        if (requests.size() > MAX_BATCH_SIZE) {
            return Flux.error(new DomainException("Cannot create more than " + MAX_BATCH_SIZE + " products at once"));
        }

        return validateSellerCanAddProducts(sellerId)
                .flatMapMany(seller -> Flux.fromIterable(requests)
                        .concatMap(request -> createProduct(sellerId, request)));
    }

    @Transactional
    public Mono<ProductResponse> updateProduct(SellerId sellerId, ProductId productId, UpdateProductRequest request) {
        log.info("Updating product: {} for seller: {}", productId, sellerId);

        return validateProductBelongsToSeller(productId, sellerId)
                .flatMap(product -> {
                    productMapper.updateDomain(product, request);
                    return productPort.updateProduct(product);
                })
                .map(productMapper::toResponse)
                .flatMap(response -> eventProducer.sendProductUpdatedEvent(sellerId, response)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .then(Mono.just(response))
                )
                .doOnSuccess(product -> log.info("Product updated successfully: {}", productId));
    }

    @Transactional
    public Mono<ProductResponse> updateProductPrice(SellerId sellerId, ProductId productId, Double mrpPrice, Double sellingPrice, Double discountPercent) {
        log.info("Updating price for product: {} for seller: {}", productId, sellerId);

        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.updatePrice(mrpPrice, sellingPrice, discountPercent);
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse)
                .flatMap(response -> eventProducer.sendProductPriceUpdatedEvent(sellerId, response)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .then(Mono.just(response))
                );
    }

    @Transactional
    public Mono<ProductResponse> updateProductQuantity(SellerId sellerId, ProductId productId, Integer quantity) {
        log.info("Updating quantity for product: {} to {} for seller: {}", productId, quantity, sellerId);

        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.updateQuantity(quantity);
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse)
                .flatMap(response -> eventProducer.sendProductQuantityUpdatedEvent(sellerId, response)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .then(Mono.just(response))
                );
    }

    @Transactional
    public Mono<ProductResponse> updateProductStatus(SellerId sellerId, ProductId productId, ProductStatus status) {
        log.info("Updating status for product: {} to {} for seller: {}", productId, status, sellerId);

        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    if (status == ProductStatus.PUBLISHED) product.publish();
                    else if (status == ProductStatus.DRAFT) product.unpublish();
                    else if (status == ProductStatus.ARCHIVED) product.archive();
                    else if (status == ProductStatus.DELETED) product.delete();
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse)
                .flatMap(response -> eventProducer.sendProductStatusUpdatedEvent(sellerId, response)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                        .then(Mono.just(response))
                );
    }

    @Transactional
    public Mono<ProductResponse> publishProduct(SellerId sellerId, ProductId productId) {
        return updateProductStatus(sellerId, productId, ProductStatus.PUBLISHED);
    }

    @Transactional
    public Mono<ProductResponse> unpublishProduct(SellerId sellerId, ProductId productId) {
        return updateProductStatus(sellerId, productId, ProductStatus.DRAFT);
    }

    public Mono<ProductResponse> getProductById(SellerId sellerId, ProductId productId) {
        return validateProductBelongsToSeller(productId, sellerId).map(productMapper::toResponse);
    }

    public Flux<ProductResponse> getProductsBySellerId(SellerId sellerId, int page, int size) {
        return productPort.getProductsBySellerId(sellerId, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getActiveProductsBySellerId(SellerId sellerId, int page, int size) {
        return productPort.getActiveProductsBySellerId(sellerId, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getProductsByStatus(SellerId sellerId, ProductStatus status, int page, int size) {
        return productPort.getProductsByStatus(sellerId, status, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getProductsByCategory(SellerId sellerId, String categoryId, int page, int size) {
        return productPort.getProductsByCategory(sellerId, categoryId, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getTopProductsBySellerId(SellerId sellerId, int limit) {
        return productPort.getTopProductsBySellerId(sellerId, limit).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getNewProductsBySellerId(SellerId sellerId, int limit) {
        return productPort.getNewProductsBySellerId(sellerId, limit).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getOnSaleProductsBySellerId(SellerId sellerId, int page, int size) {
        return productPort.getOnSaleProductsBySellerId(sellerId, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> getLowStockProductsBySellerId(SellerId sellerId, int page, int size) {
        return productPort.getLowStockProductsBySellerId(sellerId, page, size).map(productMapper::summaryToResponse);
    }

    public Flux<ProductResponse> searchProducts(SellerId sellerId, String searchTerm, int page, int size) {
        return productPort.searchProductsBySellerId(sellerId, searchTerm, page, size).map(productMapper::summaryToResponse);
    }

    @Transactional
    public Mono<Void> deleteProduct(SellerId sellerId, ProductId productId) {
        log.info("Deleting product: {} for seller: {}", productId, sellerId);

        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.delete();
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .flatMap(product -> eventProducer.sendProductDeletedEvent(sellerId, productId)
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorResume(err -> Mono.empty())
                )
                .then();
    }

    @Transactional
    public Mono<Void> deleteProducts(SellerId sellerId, List<ProductId> productIds) {
        log.info("Deleting {} products for seller: {}", productIds.size(), sellerId);

        if (productIds.size() > MAX_BATCH_SIZE) {
            return Mono.error(new DomainException("Cannot delete more than " + MAX_BATCH_SIZE + " products at once"));
        }

        return Flux.fromIterable(productIds)
                .flatMap(productId -> deleteProduct(sellerId, productId))
                .then();
    }

    @Transactional
    public Mono<Void> deleteAllProducts(SellerId sellerId) {
        log.info("Deleting all products for seller: {}", sellerId);

        return deleteNextBatch(sellerId)
                .expand(hasMoreData -> hasMoreData ? deleteNextBatch(sellerId) : Mono.empty())
                .then();
    }

    private Mono<Boolean> deleteNextBatch(SellerId sellerId) {
        return productPort.getProductsBySellerId(sellerId, 0, 50)
                .map(dto -> new ProductId(dto.id()))
                .collectList()
                .flatMap(ids -> ids.isEmpty()
                        ? Mono.just(false)
                        : deleteProducts(sellerId, ids).thenReturn(true)
                );
    }

    @Transactional
    public Flux<ProductResponse> bulkUpdateProductStatus(SellerId sellerId, List<ProductId> productIds, ProductStatus status) {
        if (productIds.size() > MAX_BATCH_SIZE) {
            return Flux.error(new DomainException("Cannot update more than " + MAX_BATCH_SIZE + " products at once"));
        }
        return Flux.fromIterable(productIds)
                .flatMap(productId -> updateProductStatus(sellerId, productId, status));
    }

    @Transactional
    public Flux<ProductResponse> bulkUpdatePrices(SellerId sellerId, List<ProductPriceUpdate> priceUpdates) {
        if (priceUpdates.size() > MAX_BATCH_SIZE) {
            return Flux.error(new DomainException("Cannot update more than " + MAX_BATCH_SIZE + " products at once"));
        }
        return Flux.fromIterable(priceUpdates)
                .flatMap(update -> updateProductPrice(
                        sellerId,
                        new ProductId(update.productId()),
                        update.mrpPrice(),
                        update.sellingPrice(),
                        update.discountPercent()
                ));
    }

    @Transactional
    public Flux<ProductResponse> bulkUpdateQuantities(SellerId sellerId, List<ProductQuantityUpdate> quantityUpdates) {
        if (quantityUpdates.size() > MAX_BATCH_SIZE) {
            return Flux.error(new DomainException("Cannot update more than " + MAX_BATCH_SIZE + " products at once"));
        }
        return Flux.fromIterable(quantityUpdates)
                .flatMap(update -> updateProductQuantity(
                        sellerId,
                        new ProductId(update.productId()),
                        update.quantity()
                ));
    }

    @Transactional
    public Mono<ProductResponse> featureProduct(SellerId sellerId, ProductId productId) {
        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.feature();
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse);
    }

    @Transactional
    public Mono<ProductResponse> unfeatureProduct(SellerId sellerId, ProductId productId) {
        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.unfeature();
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse);
    }

    @Transactional
    public Mono<ProductResponse> addProductImage(SellerId sellerId, ProductId productId, String imageUrl) {
        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.addImage(imageUrl);
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse);
    }

    @Transactional
    public Mono<ProductResponse> removeProductImage(SellerId sellerId, ProductId productId, String imageUrl) {
        return validateProductBelongsToSeller(productId, sellerId)
                .map(product -> {
                    product.removeImage(imageUrl);
                    return product;
                })
                .flatMap(productPort::updateProduct)
                .map(productMapper::toResponse);
    }


    private Mono<Seller> validateSellerCanAddProducts(SellerId sellerId) {
        return sellerRepository.findById(sellerId)
                .switchIfEmpty(Mono.error(new DomainException("Seller not found: " + sellerId)))
                .flatMap(seller -> {
                    if (!seller.canAddProducts()) {
                        return Mono.error(new DomainException("Seller cannot add products. Status: " + seller.getAccountStatus()));
                    }
                    return Mono.just(seller);
                });
    }

    private Mono<Void> validateProductLimit(SellerId sellerId) {
        return productPort.countProductsBySellerId(sellerId)
                .flatMap(count -> count >= MAX_PRODUCTS_PER_SELLER
                        ? Mono.error(new DomainException("Maximum product limit reached: " + MAX_PRODUCTS_PER_SELLER))
                        : Mono.empty()
                );
    }

    private Mono<Product> validateProductBelongsToSeller(ProductId productId, SellerId sellerId) {
        return productPort.getProductById(productId)
                .switchIfEmpty(Mono.error(new DomainException("Product not found: " + productId)))
                .flatMap(product -> !product.getSellerId().equals(sellerId)
                        ? Mono.error(new DomainException("Product does not belong to seller: " + sellerId))
                        : Mono.just(product)
                );
    }


    public record ProductPriceUpdate(
            String productId,
            Double mrpPrice,
            Double sellingPrice,
            Double discountPercent
    ) {}

    public record ProductQuantityUpdate(
            String productId,
            Integer quantity
    ) {}
}