package com.mygitgor.seller_service.domain.port.outgoing;

import com.mygitgor.seller_service.domain.model.SellerStatistics;
import reactor.core.publisher.Mono;

public interface SellerStatisticsRepositoryPort {
    Mono<SellerStatistics> getStatistics();
    Mono<SellerStatistics> refreshStatistics();
}
