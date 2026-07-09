package com.mygitgor.seller_service.infrastructure.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.response.ProductResponse;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.infrastructure.kafka.KafkaTopics;
import com.mygitgor.seller_service.infrastructure.kafka.event.*;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.ProductId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.domain.port.outgoing.KafkaEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEventProducer implements KafkaEventPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> sendSellerRegisteredEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_REGISTERED");
    }

    @Override
    public Mono<Void> sendSellerUpdatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_UPDATED");
    }

    @Override
    public Mono<Void> sendSellerDeletedEvent(SellerId sellerId, Email email) {
        var event = new SellerDeletedEvent(sellerId.toString(), email.value(), LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_DELETED");
    }

    @Override
    public Mono<Void> sendSellerActivatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_ACTIVATED");
    }

    @Override
    public Mono<Void> sendSellerSuspendedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_SUSPENDED");
    }

    @Override
    public Mono<Void> sendSellerBannedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "SELLER_BANNED");
    }

    @Override
    public Mono<Void> sendEmailVerifiedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "EMAIL_VERIFIED");
    }

    @Override
    public Mono<Void> sendEmailVerificationRequestedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "EMAIL_VERIFICATION_REQUESTED");
    }

    @Override
    public Mono<Void> sendBusinessVerifiedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "BUSINESS_VERIFIED");
    }

    @Override
    public Mono<Void> sendVerificationRejectedEvent(Seller seller, String reason) {
        var event = new VerificationRejectedEvent(seller.getSellerId().toString(), seller, reason, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "VERIFICATION_REJECTED");
    }

    @Override
    public Mono<Void> sendBusinessDetailsUpdatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "BUSINESS_DETAILS_UPDATED");
    }

    @Override
    public Mono<Void> sendBankDetailsUpdatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "BANK_DETAILS_UPDATED");
    }

    @Override
    public Mono<Void> sendRatingUpdatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "RATING_UPDATED");
    }

        @Override
    public Mono<Void> sendOrderStatsUpdatedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "ORDER_STATS_UPDATED");
    }

    @Override
    public Mono<Void> sendTaxVerifiedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "TAX_VERIFIED");
    }

    @Override
    public Mono<Void> sendDocumentsUploadedEvent(Seller seller) {
        var event = new SellerEvent(seller.getSellerId().toString(), seller, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "DOCUMENTS_UPLOADED");
    }

    @Override
    public Mono<Void> sendAddressAddedEvent(SellerId sellerId, Address address, AddressType type) {
        var event = new SellerAddressEvent(sellerId.toString(), address, type.name(), LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "ADDRESS_ADDED");    }

    @Override
    public Mono<Void> sendAddressUpdatedEvent(SellerId sellerId, Address address) {
        var event = new SellerAddressEvent(sellerId.toString(), address, "UPDATED", LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, event.sellerId(), event, "ADDRESS_UPDATED");
    }

    @Override
    public Mono<Void> sendBulkSellerUpdateEvent(List<Seller> sellers, String action) {
        var event = new BulkSellerUpdateEvent(action, sellers, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, "BULK_ACTION_" + action, event, "BULK_SELLER_UPDATE");
    }

    @Override
    public Mono<Void> sendBulkVerificationEvent(List<SellerId> sellerIds, String verifiedBy) {
        List<String> ids = sellerIds.stream().map(SellerId::toString).toList();
        var event = new BulkVerificationEvent(ids, verifiedBy, LocalDateTime.now());
        return send(KafkaTopics.SELLER_EVENTS, "BULK_VERIFY", event, "BULK_VERIFICATION");
    }

    @Override
    public Mono<Void> sendCommissionRateUpdatedEvent(Seller seller, Double oldRate, Double newRate) {
        var event = new CommissionRateUpdatedEvent(seller.getSellerId().toString(), seller, oldRate, newRate, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.sellerId(), event, "COMMISSION_RATE_UPDATED");
    }

    @Override
    public Mono<Void> sendSellerReportGeneratedEvent(SellerReport report) {
        var event = new SellerReportGeneratedEvent(report.getReportId().toString(), report.getSellerId().toString(), report, LocalDateTime.now());
        return send(KafkaTopics.SELLER_REPORTS, event.sellerId(), event, "SELLER_REPORT_GENERATED");
    }

    @Override
    public Mono<Void> sendTransactionCreatedEvent(TransactionDto transaction) {
        var event = new SellerTransactionEvent(transaction.transactionId(), transaction.sellerId(), transaction, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.transactionId(), event, "TRANSACTION_CREATED");
    }

    @Override
    public Mono<Void> sendTransactionCompletedEvent(TransactionDto transaction) {
        var event = new SellerTransactionEvent(transaction.transactionId(), transaction.sellerId(), transaction, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.transactionId(), event, "TRANSACTION_COMPLETED");
    }

    @Override
    public Mono<Void> sendTransactionFailedEvent(TransactionDto transaction) {
        var event = new SellerTransactionEvent(transaction.transactionId(), transaction.sellerId(), transaction, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.transactionId(), event, "TRANSACTION_FAILED");
    }

    @Override
    public Mono<Void> sendTransactionRefundedEvent(TransactionDto transaction) {
        var event = new SellerTransactionEvent(transaction.transactionId(), transaction.sellerId(), transaction, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.transactionId(), event, "TRANSACTION_REFUNDED");
    }

    @Override
    public Mono<Void> sendTransactionUpdatedEvent(TransactionDto transaction) {
        var event = new SellerTransactionEvent(transaction.transactionId(), transaction.sellerId(), transaction, LocalDateTime.now());
        return send(KafkaTopics.SELLER_TRANSACTIONS, event.transactionId(), event, "TRANSACTION_UPDATED");
    }

    @Override
    public Mono<Void> sendProductCreatedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_CREATED");
    }

    @Override
    public Mono<Void> sendProductUpdatedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_UPDATED");
    }

    @Override
    public Mono<Void> sendProductPriceUpdatedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_PRICE_UPDATED");
    }

    @Override
    public Mono<Void> sendProductQuantityUpdatedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_QUANTITY_UPDATED");
    }

    @Override
    public Mono<Void> sendProductStatusUpdatedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_STATUS_UPDATED");
    }

    @Override
    public Mono<Void> sendProductDeletedEvent(SellerId sellerId, ProductId productId) {
        var event = new SellerProductDeletedEvent(productId.toString(), sellerId.toString(), LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_DELETED");
    }

    @Override
    public Mono<Void> sendProductFeaturedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_FEATURED");
    }

    @Override
    public Mono<Void> sendProductUnfeaturedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_UNFEATURED");
    }

    @Override
    public Mono<Void> sendProductImageAddedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_IMAGE_ADDED");
    }

    @Override
    public Mono<Void> sendProductImageRemovedEvent(SellerId sellerId, ProductResponse product) {
        var event = new SellerProductEvent(product.id(), sellerId.toString(), product, LocalDateTime.now());
        return send(KafkaTopics.SELLER_PRODUCTS, event.productId(), event, "PRODUCT_IMAGE_REMOVED");
    }


    private Mono<Void> send(String topic, String key, Object payload, String eventType) {
        return Mono.defer(() -> {
            try {
                log.debug("Serializing and preparing event: {} with Key: {}", eventType, key);
                String jsonPayload = objectMapper.writeValueAsString(payload);

                ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, jsonPayload);
                record.headers().add("X-Event-Type", eventType.getBytes(StandardCharsets.UTF_8));

                return Mono.fromFuture(kafkaTemplate.send(record))
                        .doOnSuccess(result -> log.debug("Published Event [{}] successfully. Partition: {}, Offset: {}",
                                eventType, result.getRecordMetadata().partition(), result.getRecordMetadata().offset()))
                        .doOnError(err -> log.error("Failed to route Kafka event: {} for key: {}", eventType, key, err))
                        .then();

            } catch (JsonProcessingException e) {
                log.error("Failed serialization for event configuration type: {}", eventType, e);
                return Mono.error(e);
            }
        });
    }
}
