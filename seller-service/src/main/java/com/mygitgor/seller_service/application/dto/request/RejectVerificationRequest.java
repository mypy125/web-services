package com.mygitgor.seller_service.application.dto.request;

public record RejectVerificationRequest(
        String reason,
        String rejectedBy
) {}
