package com.mygitgor.auth_service.infrastrucrure.client.fallback;

import com.mygitgor.auth_service.domain.seller.model.Seller;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.naming.ServiceUnavailableException;

@Slf4j
@Component
public class SellerServiceFallback {

    public Mono<Boolean> existsByEmail(Email email) {
        log.warn("SellerService fallback: existsByEmail for {}", email);
        return Mono.just(false);
    }

    public Mono<Seller> getSellerByEmail(Email email) {
        log.warn("SellerService fallback: getSellerByEmail for {}", email);
        return Mono.error(new ServiceUnavailableException("Seller service is temporarily unavailable"));
    }

    public Mono<Seller> getSellerById(SellerId sellerId) {
        log.warn("SellerService fallback: getSellerById for {}", sellerId);
        return Mono.error(new ServiceUnavailableException("Seller service is temporarily unavailable"));
    }

    public Mono<Seller> getSellerByUserId(UserId userId) {
        log.warn("SellerService fallback: getSellerByUserId for {}", userId);
        return Mono.error(new ServiceUnavailableException("Seller service is temporarily unavailable"));
    }

    public Mono<Seller> createSeller(Seller seller) {
        log.warn("SellerService fallback: createSeller for {}", seller.getEmail());
        return Mono.error(new ServiceUnavailableException("Unable to create seller. Service unavailable"));
    }

    public Mono<Seller> updateSeller(Seller seller) {
        log.warn("SellerService fallback: updateSeller for {}", seller.getEmail());
        return Mono.error(new ServiceUnavailableException("Unable to update seller. Service unavailable"));
    }

    public Mono<Seller> verifySellerEmail(Email email) {
        log.warn("SellerService fallback: verifySellerEmail for {}", email);
        return Mono.error(new ServiceUnavailableException("Unable to verify seller email. Service unavailable"));
    }

    public Mono<Seller> updateAccountStatus(SellerId sellerId, String status, String reason) {
        log.warn("SellerService fallback: updateAccountStatus for {}", sellerId);
        return Mono.error(new ServiceUnavailableException("Unable to update account status. Service unavailable"));
    }
}
