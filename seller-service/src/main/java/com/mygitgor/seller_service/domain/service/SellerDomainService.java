package com.mygitgor.seller_service.domain.service;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.exception.DomainException;
import com.mygitgor.seller_service.domain.model.shared.valueobject.Email;
import com.mygitgor.seller_service.domain.repository.SellerRepositoryPort;
import com.mygitgor.seller_service.domain.specification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerDomainService {
    private final SellerRepositoryPort sellerRepository;
    private final SellerEmailUniquenessSpec emailUniquenessSpec;
    private final SellerCompositeSpec compositeSpec;
    private final SellerVerificationStatusSpec verificationStatusSpec;
    private final SellerAccountStatusSpec accountStatusSpec;
    private final SellerBankDetailsSpec bankDetailsSpec;
    private final SellerBusinessDetailsSpec businessDetailsSpec;
    private final SellerCommissionSpec commissionSpec;

    public Mono<Void> validateEmailUniqueness(Email email) {
        log.debug("Validating email uniqueness: {}", email);
        return emailUniquenessSpec.isSatisfiedBy(email)
                .flatMap(isUnique -> {
                    if (!isUnique) {
                        return Mono.error(new DomainException("Email already exists: " + email));
                    }
                    return Mono.empty();
                });
    }

    public Mono<Boolean> isReadyToSell(Seller seller) {
        return compositeSpec.isReadyToSell(seller);
    }

    public Mono<Boolean> canAddProducts(Seller seller) {
        return compositeSpec.canAddProducts(seller);
    }

    public Mono<Void> validateCommissionRate(Seller seller, Double newRate) {
        return commissionSpec.isCommissionRateValid(seller, newRate)
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.error(new DomainException("Commission rate is not valid"));
                    }
                    return Mono.empty();
                });
    }

    public Mono<Void> validateBankDetails(Seller seller) {
        return bankDetailsSpec.isBankDetailsComplete(seller)
                .flatMap(isComplete -> {
                    if (!isComplete) {
                        return Mono.error(new DomainException("Bank details are incomplete"));
                    }
                    return Mono.empty();
                });
    }

    public Mono<Seller> activateSeller(Seller seller, String activatedBy) {
        return Mono.fromCallable(() -> {
            seller.activate(activatedBy);
            return seller;
        });
    }

    public Mono<Seller> suspendSeller(Seller seller, String reason, String suspendedBy) {
        return Mono.fromCallable(() -> {
            seller.suspend(reason, suspendedBy);
            return seller;
        });
    }

    public Mono<Seller> banSeller(Seller seller, String reason, String bannedBy) {
        return Mono.fromCallable(() -> {
            seller.ban(reason, bannedBy);
            return seller;
        });
    }
}
