package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerAccountStatusSpec {

    public Mono<Boolean> isActive(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isActive = seller.isActive();
        log.debug("Seller {} is active: {}", seller.getEmail(), isActive);
        return Mono.just(isActive);
    }

    public Mono<Boolean> isBanned(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isBanned = seller.isBanned();
        log.debug("Seller {} is banned: {}", seller.getEmail(), isBanned);
        return Mono.just(isBanned);
    }

    public Mono<Boolean> isSuspended(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isSuspended = seller.isSuspended();
        log.debug("Seller {} is suspended: {}", seller.getEmail(), isSuspended);
        return Mono.just(isSuspended);
    }

    public Mono<Boolean> isNotBlocked(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean isNotBlocked = !seller.isBanned() && !seller.isSuspended();
        return Mono.just(isNotBlocked);
    }

    public Mono<Boolean> canPerformOperations(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        boolean canPerform = seller.isActive() && !seller.isBanned() && !seller.isSuspended();
        log.debug("Seller [{}] operational availability: {}", seller.getEmail(), canPerform);
        return Mono.just(canPerform);
    }
}