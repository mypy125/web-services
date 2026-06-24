package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerEmailUniquenessSpec {
    private final SellerRepositoryPort sellerRepository;

    public Mono<Boolean> isSatisfiedBy(Email email) {
        log.debug("Checking email uniqueness: {}", email);
        return sellerRepository.existsByEmail(email)
                .map(exists -> !exists)
                .doOnSuccess(isUnique -> log.debug("Email {} is unique: {}", email, isUnique));
    }

    public Mono<Boolean> isSatisfiedBy(Email email, SellerId sellerId) {
        log.debug("Checking email uniqueness for update: {}, sellerId: {}", email, sellerId);
        return sellerRepository.findByEmail(email)
                .map(seller -> seller.getSellerId().equals(sellerId))
                .defaultIfEmpty(true)
                .doOnSuccess(isUnique -> log.debug("Email {} is unique for update: {}", email, isUnique));
    }
}
