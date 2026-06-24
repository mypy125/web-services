package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.domain.model.shared.valueobject.BankDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerBankDetailsSpec {

    public Mono<Boolean> isBankDetailsComplete(Seller seller) {
        if (seller == null || seller.getBankDetails() == null) {
            return Mono.just(false);
        }

        BankDetails bank = seller.getBankDetails();
        boolean isComplete = bank.accountNumber() != null && !bank.accountNumber().isBlank()
                && bank.accountHolderName() != null && !bank.accountHolderName().isBlank()
                && bank.bankName() != null && !bank.bankName().isBlank()
                && bank.bankCode() != null && !bank.bankCode().isBlank();

        log.debug("Seller {} bank details are complete: {}", seller.getEmail(), isComplete);
        return Mono.just(isComplete);
    }

    public Mono<Boolean> canReceivePayouts(Seller seller) {
        return isBankDetailsComplete(seller)
                .flatMap(isComplete -> {
                    if (!isComplete) {
                        return Mono.just(false);
                    }
                    return Mono.just(seller.isActive() && seller.isFullyVerified());
                });
    }

    public Mono<Boolean> isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = accountNumber.matches("^[0-9]{9,18}$");
        log.debug("Account number {} is valid: {}", accountNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidIfscCode(String ifscCode) {
        if (ifscCode == null || ifscCode.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = ifscCode.matches("^[A-Za-z]{4}[0-9]{7}$");
        log.debug("IFSC code {} is valid: {}", ifscCode, isValid);
        return Mono.just(isValid);
    }
}
