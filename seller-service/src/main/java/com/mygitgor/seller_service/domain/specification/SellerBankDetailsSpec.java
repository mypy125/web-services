package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.shared.valueobject.BankDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Stream;

@Slf4j
@Component
public class SellerBankDetailsSpec {
    private static final String ACCOUNT_NUMBER_REGEX = "^[0-9]{9,18}$";
    private static final String IFSC_CODE_REGEX = "^[A-Za-z]{4}[0-9]{7}$";

    public Mono<Boolean> isBankDetailsComplete(Seller seller) {
        if (seller == null || seller.getBankDetails() == null) {
            return Mono.just(false);
        }

        BankDetails bank = seller.getBankDetails();
        boolean isComplete = Stream.of(
                bank.accountNumber(),
                bank.accountHolderName(),
                bank.bankName(),
                bank.bankCode()
        ).allMatch(field -> field != null && !field.isBlank());

        log.debug("Seller {} bank details are complete: {}", seller.getEmail(), isComplete);
        return Mono.just(isComplete);
    }

    public Mono<Boolean> canReceivePayouts(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }

        return isBankDetailsComplete(seller)
                .map(isComplete -> isComplete && seller.isActive() && seller.isFullyVerified());
    }

    public Mono<Boolean> isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = accountNumber.matches(ACCOUNT_NUMBER_REGEX);
        log.debug("Account number {} is valid: {}", accountNumber, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isValidIfscCode(String ifscCode) {
        if (ifscCode == null || ifscCode.isBlank()) {
            return Mono.just(false);
        }
        boolean isValid = ifscCode.matches(IFSC_CODE_REGEX);
        log.debug("IFSC code {} is valid: {}", ifscCode, isValid);
        return Mono.just(isValid);
    }
}
