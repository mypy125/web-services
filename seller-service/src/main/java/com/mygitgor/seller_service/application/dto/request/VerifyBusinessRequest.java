package com.mygitgor.seller_service.application.dto.request;

public record VerifyBusinessRequest(
        String verifiedBy,
        String notes
) {}
