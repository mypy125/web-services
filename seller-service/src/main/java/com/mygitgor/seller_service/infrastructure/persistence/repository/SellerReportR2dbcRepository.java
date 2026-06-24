package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerReportEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface SellerReportR2dbcRepository extends R2dbcRepository<SellerReportEntity, UUID> {
    Flux<SellerReportEntity> findAllBySellerIdAndPeriod(UUID sellerId, String period);

    @Query("SELECT * FROM seller_reports WHERE seller_id = :sellerId AND period = :period ORDER BY report_generated_at DESC LIMIT 1")
    Mono<SellerReportEntity> findLatestReport(UUID sellerId, String period);
}
