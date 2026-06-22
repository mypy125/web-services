package com.mygitgor.seller_service.domain.model.shared.valueobject;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record VerificationDocument(
        String documentType,
        String documentUrl,
        String documentName,
        String documentNumber,
        LocalDateTime issuedDate,
        LocalDateTime expiryDate,
        String verifiedBy,
        LocalDateTime verifiedAt,
        String verificationStatus,
        String rejectionReason,
        Map<String, String> metadata
) {}
