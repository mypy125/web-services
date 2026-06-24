package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface SellerR2dbcRepository extends R2dbcRepository<SellerEntity, UUID> {
    Mono<SellerEntity> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
