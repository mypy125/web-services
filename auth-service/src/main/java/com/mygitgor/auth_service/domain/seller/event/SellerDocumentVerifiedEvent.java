package com.mygitgor.auth_service.domain.seller.event;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

import java.util.UUID;

@Getter
public class SellerDocumentVerifiedEvent extends ApplicationEvent {
    private final String eventId;
    private final String sellerId;
    private final String userId;
    private final String email;
    private final String sellerName;
    private final String verifiedBy;
    private final String verifiedByRole;
    private final String documentType;
    private final String documentNumber;
    private final boolean verified;
    private final String comments;
    private final String rejectionReason;
    private final LocalDateTime verifiedAt;

    @Builder
    public SellerDocumentVerifiedEvent(Object source,
                                       String sellerId,
                                       String userId,
                                       String email,
                                       String sellerName,
                                       String verifiedBy,
                                       String verifiedByRole,
                                       String documentType,
                                       String documentNumber,
                                       boolean verified,
                                       String comments,
                                       String rejectionReason,
                                       LocalDateTime verifiedAt) {
        super(source);
        this.eventId = UUID.randomUUID().toString();
        this.sellerId = sellerId;
        this.userId = userId;
        this.email = email;
        this.sellerName = sellerName;
        this.verifiedBy = verifiedBy;
        this.verifiedByRole = verifiedByRole;
        this.documentType = documentType != null ? documentType : "BUSINESS_DOCUMENTS";
        this.documentNumber = maskDocumentNumber(documentNumber);
        this.verified = verified;
        this.comments = comments;
        this.rejectionReason = rejectionReason;
        this.verifiedAt = verifiedAt != null ? verifiedAt : LocalDateTime.now();
    }

    public static SellerDocumentVerifiedEvent success(Object source,
                                                      String sellerId,
                                                      String userId,
                                                      String email,
                                                      String sellerName,
                                                      String verifiedBy,
                                                      String verifiedByRole,
                                                      String documentType,
                                                      String documentNumber,
                                                      String comments) {
        return SellerDocumentVerifiedEvent.builder()
                .source(source)
                .sellerId(sellerId)
                .userId(userId)
                .email(email)
                .sellerName(sellerName)
                .verifiedBy(verifiedBy)
                .verifiedByRole(verifiedByRole)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .verified(true)
                .comments(comments)
                .build();
    }

    public static SellerDocumentVerifiedEvent failure(Object source,
                                                      String sellerId,
                                                      String userId,
                                                      String email,
                                                      String sellerName,
                                                      String verifiedBy,
                                                      String verifiedByRole,
                                                      String documentType,
                                                      String documentNumber,
                                                      String rejectionReason,
                                                      String comments) {
        return SellerDocumentVerifiedEvent.builder()
                .source(source)
                .sellerId(sellerId)
                .userId(userId)
                .email(email)
                .sellerName(sellerName)
                .verifiedBy(verifiedBy)
                .verifiedByRole(verifiedByRole)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .verified(false)
                .rejectionReason(rejectionReason)
                .comments(comments)
                .build();
    }

    public static SellerDocumentVerifiedEvent businessVerified(Object source,
                                                               String sellerId,
                                                               String userId,
                                                               String email,
                                                               String sellerName,
                                                               String verifiedBy,
                                                               String verifiedByRole,
                                                               String comments) {
        return SellerDocumentVerifiedEvent.builder()
                .source(source)
                .sellerId(sellerId)
                .userId(userId)
                .email(email)
                .sellerName(sellerName)
                .verifiedBy(verifiedBy)
                .verifiedByRole(verifiedByRole)
                .documentType("BUSINESS_DOCUMENTS")
                .verified(true)
                .comments(comments)
                .build();
    }

    public static SellerDocumentVerifiedEvent bankVerified(Object source,
                                                           String sellerId,
                                                           String userId,
                                                           String email,
                                                           String sellerName,
                                                           String verifiedBy,
                                                           String verifiedByRole,
                                                           String comments) {
        return SellerDocumentVerifiedEvent.builder()
                .source(source)
                .sellerId(sellerId)
                .userId(userId)
                .email(email)
                .sellerName(sellerName)
                .verifiedBy(verifiedBy)
                .verifiedByRole(verifiedByRole)
                .documentType("BANK_DETAILS")
                .verified(true)
                .comments(comments)
                .build();
    }

    public static SellerDocumentVerifiedEvent taxVerified(Object source,
                                                          String sellerId,
                                                          String userId,
                                                          String email,
                                                          String sellerName,
                                                          String verifiedBy,
                                                          String verifiedByRole,
                                                          String documentType,
                                                          String documentNumber,
                                                          String comments) {
        return SellerDocumentVerifiedEvent.builder()
                .source(source)
                .sellerId(sellerId)
                .userId(userId)
                .email(email)
                .sellerName(sellerName)
                .verifiedBy(verifiedBy)
                .verifiedByRole(verifiedByRole)
                .documentType(documentType)
                .documentNumber(documentNumber)
                .verified(true)
                .comments(comments)
                .build();
    }

    private static String maskDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isEmpty()) {
            return null;
        }
        if (documentNumber.length() < 8) {
            return "***";
        }
        return documentNumber.substring(0, 4) + "****" +
                documentNumber.substring(documentNumber.length() - 4);
    }

    public boolean isApproved() {
        return verified;
    }

    public boolean isRejected() {
        return !verified;
    }

    public boolean isBusinessDocument() {
        return "BUSINESS_DOCUMENTS".equals(documentType);
    }

    public boolean isBankDocument() {
        return "BANK_DETAILS".equals(documentType);
    }

    public boolean isTaxDocument() {
        return "GST".equals(documentType) || "PAN".equals(documentType);
    }

    @Override
    public String toString() {
        return String.format("SellerDocumentVerifiedEvent{sellerId=%s, email=%s, documentType=%s, verified=%s, verifiedAt=%s}",
                sellerId, email, documentType, verified, verifiedAt);
    }
}
