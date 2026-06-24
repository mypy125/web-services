package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.AccountStatus;
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
        boolean isActive = seller.getAccountStatus() == AccountStatus.ACTIVE;
        log.debug("Seller {} is active: {}", seller.getEmail(), isActive);
        return Mono.just(isActive);
    }

    public Mono<Boolean> isBanned(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isBanned = seller.getAccountStatus() == AccountStatus.BANNED;
        log.debug("Seller {} is banned: {}", seller.getEmail(), isBanned);
        return Mono.just(isBanned);
    }

    public Mono<Boolean> isSuspended(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean isSuspended = seller.getAccountStatus() == AccountStatus.SUSPENDED;
        log.debug("Seller {} is suspended: {}", seller.getEmail(), isSuspended);
        return Mono.just(isSuspended);
    }

    public Mono<Boolean> isNotBlocked(Seller seller) {
        return isBanned(seller)
                .flatMap(isBanned -> isSuspended(seller)
                        .map(isSuspended -> !isBanned && !isSuspended));
    }

    public Mono<Boolean> canPerformOperations(Seller seller) {
        return isActive(seller)
                .flatMap(isActive -> isNotBlocked(seller)
                        .map(isNotBlocked -> isActive && isNotBlocked));
    }
}