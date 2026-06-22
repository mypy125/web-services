package com.mygitgor.seller_service.domain.model.shared.valueobject;

import lombok.Builder;

@Builder
public record BankDetails(
        String accountNumber,
        String accountHolderName,
        String bankName,
        String bankCode,
        String branchName,
        String accountType,
        String upiId,
        String ifscCode,
        String swiftCode
) {}