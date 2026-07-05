package com.mygitgor.transaction_service.infrastructure.persistence.repository;

import com.mygitgor.transaction_service.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface TransactionR2dbcRepository extends R2dbcRepository<TransactionEntity, UUID> {
    Flux<TransactionEntity> findAllBySellerId(UUID sellerId, Pageable pageable);
    Flux<TransactionEntity> findAllBySellerIdAndStatus(UUID sellerId, String status, Pageable pageable);
    Mono<TransactionEntity> findByReferenceNumber(String referenceNumber);
    Mono<TransactionEntity> findByOrderId(UUID orderId);
}
