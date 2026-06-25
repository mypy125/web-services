package com.mygitgor.seller_service.infrastructure.persistence;

import com.mygitgor.seller_service.domain.model.SellerStatistics;
import com.mygitgor.seller_service.domain.port.outgoing.SellerStatisticsRepositoryPort;
import com.mygitgor.seller_service.infrastructure.persistence.mapper.SellerStatisticsPersistenceMapper;
import com.mygitgor.seller_service.infrastructure.persistence.repository.SellerStatisticsR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SellerStatisticsRepositoryAdapter implements SellerStatisticsRepositoryPort {
    private final SellerStatisticsR2dbcRepository repository;
    private final SellerStatisticsPersistenceMapper mapper;

    @Override
    public Mono<SellerStatistics> getStatistics() {
        return null;
    }

    @Override
    public Mono<SellerStatistics> refreshStatistics() {
        return null;
    }
}
