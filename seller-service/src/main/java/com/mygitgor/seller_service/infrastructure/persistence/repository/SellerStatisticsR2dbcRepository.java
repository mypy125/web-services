package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.shared.valueobject.id.StatisticsId;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerStatisticsEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SellerStatisticsR2dbcRepository extends ReactiveCrudRepository<SellerStatisticsEntity, UUID> {
    Mono<SellerStatisticsEntity> findById(StatisticsId id);
}
