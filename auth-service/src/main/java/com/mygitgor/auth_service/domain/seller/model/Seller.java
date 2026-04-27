package com.mygitgor.auth_service.domain.seller.model;


import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.seller.event.*;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

@Getter
@Builder
public class Seller extends AbstractAggregateRoot<Seller> {
    private final SellerId sellerId;
    private final Email email;
    private String sellerName;
    private String mobile;
    private UserRole role;
    private boolean emailVerified;
    private SellerVerificationStatus verificationStatus;
    private AccountStatus accountStatus;
    private BusinessDetails businessDetails;
    private BankDetails bankDetails;
    private Address pickupAddress;
    private String gstNumber;
    private String panNumber;
    private Double commissionRate;
    private String storeLogo;
    private String storeBanner;
    private String storeDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime businessVerifiedAt;
    private LocalDateTime lastActiveAt;
    private String rejectionReason;

    public void verifyEmail() {
        if (this.emailVerified) {
            throw new DomainException("Email already verified");
        }

        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        SellerVerificationStatus oldVerificationStatus = this.verificationStatus;
        AccountStatus oldAccountStatus = this.accountStatus;

        if (this.verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED) {
            this.verificationStatus = SellerVerificationStatus.FULLY_VERIFIED;
            this.accountStatus = AccountStatus.ACTIVE;
        } else if (this.verificationStatus == SellerVerificationStatus.PENDING) {
            this.verificationStatus = SellerVerificationStatus.EMAIL_VERIFIED;
            this.accountStatus = AccountStatus.PENDING_VERIFICATION;
        }

        registerEvent(SellerEmailVerifiedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .email(this.email.toString())
                .verifiedAt(this.emailVerifiedAt)
                .build());

        registerEvent(SellerVerificationStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .oldStatus(oldVerificationStatus)
                .newStatus(this.verificationStatus)
                .reason("Email verified")
                .changedBy("SYSTEM")
                .notes("Email verification completed")
                .build());

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldAccountStatus)
                .newStatus(this.accountStatus)
                .reason("Email verification completed")
                .changedBy("SYSTEM")
                .requiresNotification(false)
                .build());
    }

    public void verifyBusiness(String verifiedBy, String verifiedByRole, String notes) {
        if (this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED) {
            throw new DomainException("Business already fully verified");
        }

        this.businessVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        SellerVerificationStatus oldVerificationStatus = this.verificationStatus;
        AccountStatus oldAccountStatus = this.accountStatus;

        if (this.emailVerified) {
            this.verificationStatus = SellerVerificationStatus.FULLY_VERIFIED;
            this.accountStatus = AccountStatus.ACTIVE;
        } else {
            this.verificationStatus = SellerVerificationStatus.BUSINESS_VERIFIED;
            this.accountStatus = AccountStatus.PENDING_VERIFICATION;
        }

        registerEvent(SellerDocumentVerifiedEvent.businessVerified(
                this,
                this.sellerId.toString(),
                this.getUserId(),
                this.email.toString(),
                this.sellerName,
                verifiedBy,
                verifiedByRole,
                notes
        ));

        registerEvent(SellerVerificationStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .oldStatus(oldVerificationStatus)
                .newStatus(this.verificationStatus)
                .reason("Business documents verified")
                .changedBy(verifiedBy)
                .changedByRole(verifiedByRole)
                .notes(notes)
                .build());

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldAccountStatus)
                .newStatus(this.accountStatus)
                .reason("Business documents verified")
                .changedBy(verifiedBy)
                .changedByRole(verifiedByRole)
                .notes(notes)
                .requiresNotification(true)
                .build());
    }

    public void rejectVerification(String reason, String rejectedBy) {
        SellerVerificationStatus oldVerificationStatus = this.verificationStatus;
        AccountStatus oldAccountStatus = this.accountStatus;

        this.verificationStatus = SellerVerificationStatus.REJECTED;
        this.accountStatus = AccountStatus.SUSPENDED;
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();

        // Событие для изменения статуса верификации
        registerEvent(SellerVerificationStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .oldStatus(oldVerificationStatus)
                .newStatus(SellerVerificationStatus.REJECTED)
                .reason(reason)
                .changedBy(rejectedBy)
                .notes("Verification rejected: " + reason)
                .build());

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldAccountStatus)
                .newStatus(AccountStatus.SUSPENDED)
                .reason(reason)
                .changedBy(rejectedBy)
                .notes("Account suspended due to verification rejection")
                .requiresNotification(true)
                .build());
    }

    public void updateProfile(String sellerName, String mobile, String storeDescription,
                              String storeLogo, String storeBanner) {
        String oldSellerName = this.sellerName;
        String oldMobile = this.mobile;
        String oldStoreDescription = this.storeDescription;

        if (sellerName != null && !sellerName.isBlank()) {
            this.sellerName = sellerName;
        }

        if (mobile != null && !mobile.isBlank()) {
            this.mobile = mobile;
        }

        if (storeDescription != null) {
            this.storeDescription = storeDescription;
        }

        if (storeLogo != null) {
            this.storeLogo = storeLogo;
        }

        if (storeBanner != null) {
            this.storeBanner = storeBanner;
        }

        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerProfileUpdatedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .oldSellerName(oldSellerName)
                .newSellerName(this.sellerName)
                .oldMobile(oldMobile)
                .newMobile(this.mobile)
                .oldStoreDescription(oldStoreDescription)
                .newStoreDescription(this.storeDescription)
                .updatedAt(this.updatedAt)
                .build());
    }

    public void updateBusinessDetails(BusinessDetails newDetails, String updatedBy) {
        BusinessDetails oldDetails = this.businessDetails;
        this.businessDetails = newDetails;
        this.updatedAt = LocalDateTime.now();

        if (this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED ||
                this.verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED) {
            SellerVerificationStatus oldStatus = this.verificationStatus;
            this.verificationStatus = SellerVerificationStatus.EMAIL_VERIFIED;
            this.accountStatus = AccountStatus.PENDING_VERIFICATION;

            registerEvent(SellerVerificationStatusChangedEvent.builder()
                    .source(this)
                    .sellerId(this.sellerId.toString())
                    .userId(this.getUserId())
                    .email(this.email.toString())
                    .sellerName(this.sellerName)
                    .oldStatus(oldStatus)
                    .newStatus(this.verificationStatus)
                    .reason("Business details updated - verification reset")
                    .changedBy(updatedBy)
                    .build());
        }

        registerEvent(SellerBusinessUpdatedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .oldBusinessName(oldDetails != null ? oldDetails.getBusinessName() : null)
                .newBusinessName(newDetails.getBusinessName())
                .updatedAt(this.updatedAt)
                .build());
    }

    public void suspend(String reason, String suspendedBy) {
        if (this.accountStatus == AccountStatus.SUSPENDED) {
            throw new DomainException("Seller already suspended");
        }
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot suspend banned seller");
        }

        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldStatus)
                .newStatus(AccountStatus.SUSPENDED)
                .reason(reason)
                .changedBy(suspendedBy)
                .notes("Seller account suspended")
                .requiresNotification(true)
                .build());
    }

    public void activate(String activatedBy) {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("Seller already active");
        }

        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldStatus)
                .newStatus(AccountStatus.ACTIVE)
                .reason("Seller activated")
                .changedBy(activatedBy)
                .notes("Account activated")
                .requiresNotification(true)
                .build());
    }

    public void ban(String reason, String bannedBy) {
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Seller already banned");
        }

        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = AccountStatus.BANNED;
        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerStatusChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .userId(this.getUserId())
                .email(this.email.toString())
                .sellerName(this.sellerName)
                .role(this.role)
                .oldStatus(oldStatus)
                .newStatus(AccountStatus.BANNED)
                .reason(reason)
                .changedBy(bannedBy)
                .notes("Seller account banned")
                .requiresNotification(true)
                .build());
    }

    private String getUserId() {
        return null;
    }

    public void updateBusinessDetails(BusinessDetails newDetails) {
        BusinessDetails oldDetails = this.businessDetails;
        this.businessDetails = newDetails;
        this.updatedAt = LocalDateTime.now();

        if (this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED ||
                this.verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED) {
            this.verificationStatus = SellerVerificationStatus.EMAIL_VERIFIED;
            this.accountStatus = AccountStatus.PENDING_VERIFICATION;
        }

        registerEvent(SellerBusinessUpdatedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .oldBusinessName(oldDetails != null ? oldDetails.getBusinessName() : null)
                .newBusinessName(newDetails.getBusinessName())
                .updatedAt(this.updatedAt)
                .build());
    }

    public void updateBankDetails(BankDetails newBankDetails) {
        this.bankDetails = newBankDetails;
        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerBankDetailsUpdatedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .updatedAt(this.updatedAt)
                .build());
    }

    public void updatePickupAddress(Address newAddress) {
        this.pickupAddress = newAddress;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTaxInfo(String gstNumber, String panNumber) {
        this.gstNumber = gstNumber;
        this.panNumber = panNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public void updateCommissionRate(double newRate, String updatedBy) {
        if (newRate < 0 || newRate > 100) {
            throw new DomainException("Commission rate must be between 0 and 100");
        }

        double oldRate = this.commissionRate;
        this.commissionRate = newRate;
        this.updatedAt = LocalDateTime.now();

        registerEvent(SellerCommissionRateChangedEvent.builder()
                .source(this)
                .sellerId(this.sellerId.toString())
                .oldRate(oldRate)
                .newRate(newRate)
                .updatedBy(updatedBy)
                .updatedAt(this.updatedAt)
                .build());
    }

    public boolean canSell() {
        return this.accountStatus == AccountStatus.ACTIVE
                && this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED;
    }

    public boolean canAddProducts() {
        return canSell() && this.businessDetails != null && this.bankDetails != null;
    }

    public boolean isPendingVerification() {
        return this.verificationStatus == SellerVerificationStatus.PENDING
                || this.verificationStatus == SellerVerificationStatus.EMAIL_VERIFIED
                || this.verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED;
    }

    public boolean isFullyVerified() {
        return this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED;
    }

    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public AccountStatus getCurrentAccountStatus() {
        return this.accountStatus;
    }

    public SellerVerificationStatus getCurrentVerificationStatus() {
        return this.verificationStatus;
    }

    public static Seller register(Email email, String sellerName, String mobile,
                                  BusinessDetails businessDetails, BankDetails bankDetails,
                                  Address pickupAddress) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .sellerName(sellerName)
                .mobile(mobile)
                .role(UserRole.ROLE_SELLER)
                .emailVerified(false)
                .verificationStatus(SellerVerificationStatus.PENDING)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .businessDetails(businessDetails)
                .bankDetails(bankDetails)
                .pickupAddress(pickupAddress)
                .commissionRate(5.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Seller registerQuick(Email email, String sellerName, String mobile) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .sellerName(sellerName)
                .mobile(mobile)
                .role(UserRole.ROLE_SELLER)
                .emailVerified(false)
                .verificationStatus(SellerVerificationStatus.PENDING)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .commissionRate(5.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Override
    public String toString() {
        return String.format("Seller{sellerId=%s, email=%s, sellerName=%s, accountStatus=%s, verificationStatus=%s}",
                sellerId, email, sellerName, accountStatus, verificationStatus);
    }
}
