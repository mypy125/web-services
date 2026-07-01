package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.Transaction;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.type.AddressType;
import reactor.core.publisher.Mono;

import java.util.List;

public interface KafkaEventPort {
    Mono<Void> sendSellerRegisteredEvent(Seller seller);
    Mono<Void> sendSellerUpdatedEvent(Seller seller);
    Mono<Void> sendSellerDeletedEvent(SellerId sellerId, Email email);
    Mono<Void> sendSellerActivatedEvent(Seller seller);
    Mono<Void> sendSellerSuspendedEvent(Seller seller);
    Mono<Void> sendSellerBannedEvent(Seller seller);
    Mono<Void> sendEmailVerifiedEvent(Seller seller);
    Mono<Void> sendBusinessVerifiedEvent(Seller seller);
    Mono<Void> sendVerificationRejectedEvent(Seller seller, String reason);
    Mono<Void> sendBusinessDetailsUpdatedEvent(Seller seller);
    Mono<Void> sendBankDetailsUpdatedEvent(Seller seller);
    Mono<Void> sendCommissionRateUpdatedEvent(Seller seller, Double oldRate, Double newRate);
    Mono<Void> sendOrderStatsUpdatedEvent(Seller seller);
    Mono<Void> sendRatingUpdatedEvent(Seller seller);
    Mono<Void> sendTaxVerifiedEvent(Seller seller);
    Mono<Void> sendSellerReportGeneratedEvent(SellerReport report);
    Mono<Void> sendTransactionCreatedEvent(Transaction transaction);
    Mono<Void> sendTransactionUpdatedEvent(Transaction transaction);
    Mono<Void> sendBulkSellerUpdateEvent(List<Seller> sellers, String action);
    Mono<Void> sendBulkVerificationEvent(List<SellerId> sellerIds, String verifiedBy);
    Mono<Void> sendAddressAddedEvent(SellerId sellerId, Address address, AddressType type);
    Mono<Void> sendAddressUpdatedEvent(SellerId sellerId, Address address);
    Mono<Void> sendProductCreatedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductUpdatedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductPriceUpdatedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductQuantityUpdatedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductStatusUpdatedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductDeletedEvent(SellerId sellerId, ProductId productId);
    Mono<Void> sendProductFeaturedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductUnfeaturedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductImageAddedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendProductImageRemovedEvent(SellerId sellerId, ProductResponse product);
    Mono<Void> sendEmailVerificationRequestedEvent(Seller seller);
    Mono<Void> sendDocumentsUploadedEvent(Seller seller);
}
