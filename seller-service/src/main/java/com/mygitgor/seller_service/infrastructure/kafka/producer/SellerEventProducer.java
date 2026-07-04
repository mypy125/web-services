package com.mygitgor.seller_service.infrastructure.kafka.producer;

import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.Transaction;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.port.outgoing.KafkaEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventProducer implements KafkaEventPort {
    @Override
    public Mono<Void> sendSellerRegisteredEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerDeletedEvent(SellerId sellerId, Email email) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerActivatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerSuspendedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerBannedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendEmailVerifiedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendBusinessVerifiedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendVerificationRejectedEvent(Seller seller, String reason) {
        return null;
    }

    @Override
    public Mono<Void> sendBusinessDetailsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendBankDetailsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendCommissionRateUpdatedEvent(Seller seller, Double oldRate, Double newRate) {
        return null;
    }

    @Override
    public Mono<Void> sendOrderStatsUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendRatingUpdatedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendTaxVerifiedEvent(Seller seller) {
        return null;
    }

    @Override
    public Mono<Void> sendSellerReportGeneratedEvent(SellerReport report) {
        return null;
    }

    @Override
    public Mono<Void> sendTransactionCreatedEvent(Transaction transaction) {
        return null;
    }

    @Override
    public Mono<Void> sendTransactionUpdatedEvent(Transaction transaction) {
        return null;
    }

    @Override
    public Mono<Void> sendBulkSellerUpdateEvent(List<Seller> sellers, String action) {
        return null;
    }

    @Override
    public Mono<Void> sendBulkVerificationEvent(List<SellerId> sellerIds, String verifiedBy) {
        return null;
    }

    @Override
    public Mono<Void> sendAddressAddedEvent(SellerId sellerId, Address address, AddressType type) {
        return null;
    }

    @Override
    public Mono<Void> sendAddressUpdatedEvent(SellerId sellerId, Address address) {
        return null;
    }

    @Override
    public Mono<Void> sendProductCreatedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductUpdatedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductPriceUpdatedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductQuantityUpdatedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductStatusUpdatedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductDeletedEvent(SellerId sellerId, ProductId productId) {
        return null;
    }

    @Override
    public Mono<Void> sendProductFeaturedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductUnfeaturedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductImageAddedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }

    @Override
    public Mono<Void> sendProductImageRemovedEvent(SellerId sellerId, ProductResponse product) {
        return null;
    }
}
