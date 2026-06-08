package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
public record BankDetails(String accountNumber, String accountHolderName, String bankName, String bankCode,
                          String accountType, String upiId) {
    public BankDetails {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new DomainException("Account number is required");
        }
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new DomainException("Account holder name is required");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new DomainException("Bank name is required");
        }
        if (bankCode == null || bankCode.isBlank()) {
            throw new DomainException("Bank code is required");
        }

    }
}
