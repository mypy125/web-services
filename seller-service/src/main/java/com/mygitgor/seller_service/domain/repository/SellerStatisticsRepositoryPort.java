package com.mygitgor.seller_service.domain.repository;

import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import reactor.core.publisher.Mono;

public interface SellerStatisticsRepositoryPort {
    Mono<SellerStatistics> getStatistics();
    Mono<SellerStatistics> refreshStatistics();
    Mono<SellerStatistics> getGlobalStatistics();
}
