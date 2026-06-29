package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.request.CreateProductRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateProductRequest;
import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.application.service.SellerProductService;
import com.mygitgor.seller_service.shared.valueobject.ProductStatus;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/products")
@RequiredArgsConstructor
public class SellerProductController {
    private final SellerProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductResponse> createProduct(@PathVariable String sellerId,
                                               @Valid @RequestBody CreateProductRequest request
    ) {
        log.info("REST request to create product for seller: {}", sellerId);
        return productService.createProduct(new SellerId(sellerId), request);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<ProductResponse> createProducts(@PathVariable String sellerId,
                                                @Valid @RequestBody List<CreateProductRequest> requests
    ) {
        log.info("REST request to bulk create {} products for seller: {}", requests.size(), sellerId);
        return productService.createProducts(new SellerId(sellerId), requests);
    }

    @PutMapping("/{productId}")
    public Mono<ProductResponse> updateProduct(@PathVariable String sellerId,
                                               @PathVariable String productId,
                                               @Valid @RequestBody UpdateProductRequest request
    ) {
        log.info("REST request to update product: {} for seller: {}", productId, sellerId);
        return productService.updateProduct(new SellerId(sellerId), new ProductId(productId), request);
    }

    @PatchMapping("/{productId}/price")
    public Mono<ProductResponse> updateProductPrice(@PathVariable String sellerId,
                                                    @PathVariable String productId,
                                                    @RequestParam Double mrpPrice,
                                                    @RequestParam Double sellingPrice,
                                                    @RequestParam Double discountPercent
    ) {
        log.info("REST request to update price for product: {}", productId);
        return productService.updateProductPrice(
                new SellerId(sellerId), new ProductId(productId), mrpPrice, sellingPrice, discountPercent);
    }

    @PatchMapping("/{productId}/quantity")
    public Mono<ProductResponse> updateProductQuantity(@PathVariable String sellerId,
                                                       @PathVariable String productId,
                                                       @RequestParam Integer quantity
    ) {
        log.info("REST request to update quantity for product: {} to {}", productId, quantity);
        return productService.updateProductQuantity(new SellerId(sellerId), new ProductId(productId), quantity);
    }

    @PatchMapping("/{productId}/status")
    public Mono<ProductResponse> updateProductStatus(@PathVariable String sellerId,
                                                     @PathVariable String productId,
                                                     @RequestParam ProductStatus status
    ) {
        log.info("REST request to update status for product: {} to {}", productId, status);
        return productService.updateProductStatus(new SellerId(sellerId), new ProductId(productId), status);
    }
    
    @PatchMapping("/bulk/status")
    public Flux<ProductResponse> bulkUpdateStatus(@PathVariable String sellerId,
                                                  @RequestBody List<String> productIds,
                                                  @RequestParam ProductStatus status
    ) {
        log.info("REST request to bulk update status to {} for {} products", status, productIds.size());
        List<ProductId> domainProductIds = productIds.stream().map(ProductId::new).toList();
        return productService.bulkUpdateProductStatus(new SellerId(sellerId), domainProductIds, status);
    }

    @PatchMapping("/bulk/prices")
    public Flux<ProductResponse> bulkUpdatePrices(@PathVariable String sellerId,
                                                  @RequestBody List<SellerProductService.ProductPriceUpdate> priceUpdates
    ) {
        log.info("REST request to bulk update prices for {} products", priceUpdates.size());
        return productService.bulkUpdatePrices(new SellerId(sellerId), priceUpdates);
    }

    @PostMapping("/{productId}/featured")
    public Mono<ProductResponse> featureProduct(@PathVariable String sellerId,
                                                @PathVariable String productId
    ) {
        return productService.featureProduct(new SellerId(sellerId), new ProductId(productId));
    }

    @DeleteMapping("/{productId}/featured")
    public Mono<ProductResponse> unfeatureProduct(@PathVariable String sellerId,
                                                  @PathVariable String productId
    ) {
        return productService.unfeatureProduct(new SellerId(sellerId), new ProductId(productId));
    }

    @PostMapping("/{productId}/images")
    public Mono<ProductResponse> addProductImage(@PathVariable String sellerId,
                                                 @PathVariable String productId,
                                                 @RequestParam String imageUrl
    ) {
        return productService.addProductImage(new SellerId(sellerId), new ProductId(productId), imageUrl);
    }

    @DeleteMapping("/{productId}/images")
    public Mono<ProductResponse> removeProductImage(@PathVariable String sellerId,
                                                    @PathVariable String productId,
                                                    @RequestParam String imageUrl
    ) {
        return productService.removeProductImage(new SellerId(sellerId), new ProductId(productId), imageUrl);
    }

    @GetMapping("/{productId}")
    public Mono<ProductResponse> getProductById(@PathVariable String sellerId,
                                                @PathVariable String productId
    ) {
        return productService.getProductById(new SellerId(sellerId), new ProductId(productId));
    }

    @GetMapping
    public Flux<ProductResponse> getProducts(@PathVariable String sellerId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(required = false) ProductStatus status,
                                             @RequestParam(required = false) String categoryId,
                                             @RequestParam(required = false) String searchTerm
    ) {

        SellerId id = new SellerId(sellerId);

        if (searchTerm != null && !searchTerm.isBlank()) {
            return productService.searchProducts(id, searchTerm, page, size);
        }
        if (status != null) {
            return productService.getProductsByStatus(id, status, page, size);
        }
        if (categoryId != null) {
            return productService.getProductsByCategory(id, categoryId, page, size);
        }

        return productService.getProductsBySellerId(id, page, size);
    }

    @GetMapping("/active")
    public Flux<ProductResponse> getActiveProducts(@PathVariable String sellerId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size
    ) {
        return productService.getActiveProductsBySellerId(new SellerId(sellerId), page, size);
    }

    @GetMapping("/low-stock")
    public Flux<ProductResponse> getLowStockProducts(@PathVariable String sellerId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size
    ) {
        return productService.getLowStockProductsBySellerId(new SellerId(sellerId), page, size);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteProduct(@PathVariable String sellerId, @PathVariable String productId) {
        log.info("REST request to delete product: {}", productId);
        return productService.deleteProduct(new SellerId(sellerId), new ProductId(productId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllProducts(@PathVariable String sellerId) {
        log.info("REST request to delete ALL products for seller: {}", sellerId);
        return productService.deleteAllProducts(new SellerId(sellerId));
    }
}
