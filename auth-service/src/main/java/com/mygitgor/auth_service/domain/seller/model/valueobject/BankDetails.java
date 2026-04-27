package com.mygitgor.auth_service.domain.seller.model.valueobject;

import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class BankDetails {
    private final String accountNumber;
    private final String accountHolderName;
    private final String bankName;
    private final String bankCode;
    private final String accountType;
    private final String upiId;

    public BankDetails(String accountNumber, String accountHolderName, String bankName,
                       String bankCode, String accountType, String upiId) {
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

        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.accountType = accountType;
        this.upiId = upiId;
    }
}
