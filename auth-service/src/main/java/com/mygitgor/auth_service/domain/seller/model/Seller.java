package com.mygitgor.auth_service.domain.seller.model;

import com.mygitgor.auth_service.domain.auth.model.enums.AccountStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Seller {
    private final SellerId sellerId;
    private final Email email;
    private String sellerName;
    private UserId userId;
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
    }

    public void rejectVerification(String reason, String rejectedBy) {
        SellerVerificationStatus oldVerificationStatus = this.verificationStatus;
        AccountStatus oldAccountStatus = this.accountStatus;

        this.verificationStatus = SellerVerificationStatus.REJECTED;
        this.accountStatus = AccountStatus.SUSPENDED;
        this.rejectionReason = reason;
        this.updatedAt = LocalDateTime.now();
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
        }
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
    }

    public void activate(String activatedBy) {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("Seller already active");
        }

        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void ban(String reason, String bannedBy) {
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Seller already banned");
        }

        AccountStatus oldStatus = this.accountStatus;
        this.accountStatus = AccountStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
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
    }

    public void updateBankDetails(BankDetails newBankDetails) {
        this.bankDetails = newBankDetails;
        this.updatedAt = LocalDateTime.now();
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

    public static Seller register(Email email, UserId userId, String sellerName, String mobile,
                                  BusinessDetails businessDetails, BankDetails bankDetails,
                                  Address pickupAddress) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .userId(userId)
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

    public static Seller register(Email email, UserId userId, String sellerName, String mobile,
                                  BusinessDetails businessDetails) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .userId(userId)
                .sellerName(sellerName)
                .mobile(mobile)
                .role(UserRole.ROLE_SELLER)
                .emailVerified(false)
                .verificationStatus(SellerVerificationStatus.PENDING)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .businessDetails(businessDetails)
                .bankDetails(null)
                .pickupAddress(null)
                .commissionRate(5.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Seller registerQuick(Email email, String sellerName, String mobile, UserId userId) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .userId(userId)
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
