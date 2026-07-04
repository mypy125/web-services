package com.mygitgor.seller_service.domain.model;

import com.mygitgor.seller_service.domain.model.status.AccountStatus;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import com.mygitgor.seller_service.shared.exception.DomainException;
import com.mygitgor.seller_service.shared.valueobject.*;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.type.AddressType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class Seller {
    // TODO: Identification
    private final SellerId sellerId;
    private final Email email;

    // TODO: Basic Info
    private String sellerName;
    private String storeName;
    private String displayName;
    private String mobile;
    private String phoneNumber;
    private String profileImage;
    private String coverImage;

    // TODO: Business Info
    private BusinessDetails businessDetails;
    private BankDetails bankDetails;
    private Address pickupAddress;
    private Address returnAddress;
    private List<Address> warehouseAddresses;

    // TODO: Tax and Legal
    private String gstNumber;
    private String panNumber;
    private String tinNumber;
    private String businessRegistrationNumber;
    private boolean taxInfoVerified;
    private LocalDateTime taxInfoVerifiedAt;

    // TODO: Verification Status
    private boolean emailVerified;
    private SellerVerificationStatus verificationStatus;
    private AccountStatus accountStatus;
    private VerificationDocument verificationDocument;
    private String rejectionReason;
    private LocalDateTime rejectedAt;

    private UserRole role;

    // TODO: Commission Settings
    private Double commissionRate;
    private Double minimumCommissionRate;
    private Double maximumCommissionRate;
    private Double cashbackRate;
    private boolean autoAcceptOrders;
    private boolean autoConfirmDelivery;
    private Integer processingTimeDays;
    private Integer shippingTimeDays;
    private Double freeShippingThreshold;
    private Double domesticShippingCost;
    private Double internationalShippingCost;

    // TODO: Store
    private String storeLogo;
    private String storeBanner;
    private String storeDescription;
    private String storeTagline;
    private String storeWebsite;
    private String storeEmail;
    private String storePhone;
    private String socialMediaLinks;
    private StoreCategory storeCategory;
    private List<StoreCategory> storeCategories;

    // TODO: Ratings and Reviews
    private Double averageRating;
    private Integer totalReviews;
    private Integer totalPositiveReviews;
    private Integer totalNeutralReviews;
    private Integer totalNegativeReviews;
    private Double responseRate;
    private Double responseTimeHours;
    private Integer followersCount;

    // TODO: Performance and Metrics
    private Integer totalOrders;
    private Integer totalProducts;
    private Integer totalActiveProducts;
    private Integer totalOutOfStockProducts;
    private Double totalEarnings;
    private Double totalSales;
    private Double totalRefunds;
    private Double totalTax;
    private Double netEarnings;
    private Double averageOrderValue;
    private Double conversionRate;
    private Double returnRate;
    private Double cancellationRate;
    private Integer canceledOrders;
    private Integer totalTransactions;
    private Double totalCommissionPaid;

    //TODO: Time-Stamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime businessVerifiedAt;
    private LocalDateTime lastActiveAt;
    private LocalDateTime commissionUpdatedAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime bannedAt;
    private LocalDateTime reactivatedAt;

    private BusinessHours businessHours;

    public static Seller register(
            Email email,
            String sellerName,
            String mobile,
            BusinessDetails businessDetails,
            BankDetails bankDetails,
            Address pickupAddress
    ) {
        LocalDateTime now = LocalDateTime.now();

        return Seller.builder()
                .sellerId(new SellerId())
                .email(email)
                .sellerName(sellerName)
                .storeName(sellerName)
                .displayName(sellerName)
                .mobile(mobile)
                .phoneNumber(mobile)
                .businessDetails(businessDetails)
                .bankDetails(bankDetails)
                .pickupAddress(pickupAddress)
                .returnAddress(pickupAddress)
                .warehouseAddresses(new ArrayList<>())
                .role(UserRole.ROLE_SELLER)
                .emailVerified(false)
                .verificationStatus(SellerVerificationStatus.PENDING)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .commissionRate(5.0)
                .minimumCommissionRate(0.0)
                .maximumCommissionRate(50.0)
                .cashbackRate(0.0)
                .autoAcceptOrders(true)
                .autoConfirmDelivery(false)
                .processingTimeDays(2)
                .shippingTimeDays(3)
                .freeShippingThreshold(100.0)
                .domesticShippingCost(0.0)
                .internationalShippingCost(0.0)
                .storeCategory(StoreCategory.GENERAL)
                .storeCategories(new ArrayList<>())
                .averageRating(0.0)
                .totalReviews(0)
                .totalPositiveReviews(0)
                .totalNeutralReviews(0)
                .totalNegativeReviews(0)
                .responseRate(100.0)
                .responseTimeHours(24.0)
                .followersCount(0)
                .totalOrders(0)
                .totalProducts(0)
                .totalActiveProducts(0)
                .totalOutOfStockProducts(0)
                .totalEarnings(0.0)
                .totalSales(0.0)
                .totalRefunds(0.0)
                .totalTax(0.0)
                .netEarnings(0.0)
                .averageOrderValue(0.0)
                .conversionRate(0.0)
                .returnRate(0.0)
                .cancellationRate(0.0)
                .canceledOrders(0)
                .totalTransactions(0)
                .totalCommissionPaid(0.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void verifyEmail() {
        if (this.emailVerified) {
            throw new DomainException("Email already verified");
        }
        this.emailVerified = true;
        this.emailVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.verificationStatus == SellerVerificationStatus.PENDING) {
            this.verificationStatus = SellerVerificationStatus.EMAIL_VERIFIED;
        }
        if (this.accountStatus == AccountStatus.PENDING_VERIFICATION) {
            this.accountStatus = AccountStatus.PENDING_VERIFICATION;
        }
    }

    public void verifyBusiness(String verifiedBy, String notes) {
        if (this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED) {
            throw new DomainException("Business already fully verified");
        }

        this.verificationStatus = SellerVerificationStatus.FULLY_VERIFIED;
        this.businessVerifiedAt = LocalDateTime.now();
        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void rejectVerification(String reason, String rejectedBy) {
        this.verificationStatus = SellerVerificationStatus.REJECTED;
        this.accountStatus = AccountStatus.SUSPENDED;
        this.rejectionReason = reason;
        this.rejectedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void verifyTaxInfo(String verifiedBy) {
        this.taxInfoVerified = true;
        this.taxInfoVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate(String activatedBy) {
        if (this.accountStatus == AccountStatus.ACTIVE) {
            throw new DomainException("Seller already active");
        }
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot activate banned seller");
        }

        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        this.reactivatedAt = LocalDateTime.now();
    }

    public void suspend(String reason, String suspendedBy) {
        if (this.accountStatus == AccountStatus.SUSPENDED) {
            throw new DomainException("Seller already suspended");
        }
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Cannot suspend banned seller");
        }

        this.accountStatus = AccountStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void ban(String reason, String bannedBy) {
        if (this.accountStatus == AccountStatus.BANNED) {
            throw new DomainException("Seller already banned");
        }

        this.accountStatus = AccountStatus.BANNED;
        this.bannedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String sellerName, String displayName, String mobile,
                              String phoneNumber, String profileImage, String coverImage) {
        if (sellerName != null && !sellerName.isBlank()) {
            this.sellerName = sellerName;
        }
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName;
        }
        if (mobile != null && !mobile.isBlank()) {
            this.mobile = mobile;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            this.phoneNumber = phoneNumber;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
        if (coverImage != null) {
            this.coverImage = coverImage;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStore(String storeName, String storeDescription, String storeTagline,
                            String storeLogo, String storeBanner, String storeWebsite,
                            String storeEmail, String storePhone) {
        if (storeName != null && !storeName.isBlank()) {
            this.storeName = storeName;
        }
        if (storeDescription != null) {
            this.storeDescription = storeDescription;
        }
        if (storeTagline != null) {
            this.storeTagline = storeTagline;
        }
        if (storeLogo != null) {
            this.storeLogo = storeLogo;
        }
        if (storeBanner != null) {
            this.storeBanner = storeBanner;
        }
        if (storeWebsite != null) {
            this.storeWebsite = storeWebsite;
        }
        if (storeEmail != null) {
            this.storeEmail = storeEmail;
        }
        if (storePhone != null) {
            this.storePhone = storePhone;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateSocialMediaLinks(String socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBusinessDetails(BusinessDetails newDetails) {
        this.businessDetails = newDetails;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBankDetails(BankDetails newBankDetails) {
        this.bankDetails = newBankDetails;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePickupAddress(Address newAddress) {
        if (newAddress == null) {
            throw new DomainException("Pickup address cannot be null");
        }
        if (newAddress.addressType() != AddressType.PICKUP) {
            throw new DomainException("Address type must be PICKUP");
        }
        this.pickupAddress = newAddress;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateReturnAddress(Address newAddress) {
        if (newAddress == null) {
            throw new DomainException("Return address cannot be null");
        }
        if (newAddress.addressType() != AddressType.RETURN) {
            throw new DomainException("Address type must be RETURN");
        }
        this.returnAddress = newAddress;
        this.updatedAt = LocalDateTime.now();
    }

    public void addWarehouseAddress(Address warehouseAddress) {
        if (warehouseAddress == null) {
            throw new DomainException("Warehouse address cannot be null");
        }
        if (warehouseAddress.addressType() != AddressType.WAREHOUSE) {
            throw new DomainException("Address type must be WAREHOUSE");
        }
        if (this.warehouseAddresses == null) {
            this.warehouseAddresses = new ArrayList<>();
        }
        this.warehouseAddresses.add(warehouseAddress);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeWarehouseAddress(Address warehouseAddress) {
        if (this.warehouseAddresses != null) {
            this.warehouseAddresses.remove(warehouseAddress);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void updateTaxInfo(String gstNumber, String panNumber, String tinNumber, String registrationNumber) {
        if (gstNumber != null && !gstNumber.isBlank()) {
            this.gstNumber = gstNumber;
        }
        if (panNumber != null && !panNumber.isBlank()) {
            this.panNumber = panNumber;
        }
        if (tinNumber != null && !tinNumber.isBlank()) {
            this.tinNumber = tinNumber;
        }
        if (registrationNumber != null && !registrationNumber.isBlank()) {
            this.businessRegistrationNumber = registrationNumber;
        }
        this.taxInfoVerified = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCommissionRate(double newRate, String updatedBy) {
        if (newRate < this.minimumCommissionRate || newRate > this.maximumCommissionRate) {
            throw new DomainException("Commission rate must be between " +
                    minimumCommissionRate + " and " + maximumCommissionRate);
        }
        this.commissionRate = newRate;
        this.commissionUpdatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCashbackRate(Double cashbackRate) {
        if (cashbackRate < 0 || cashbackRate > 10) {
            throw new DomainException("Cashback rate must be between 0 and 10%");
        }
        this.cashbackRate = cashbackRate;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateShippingSettings(
            Integer processingTimeDays,
            Integer shippingTimeDays,
            Double freeShippingThreshold,
            Double domesticShippingCost,
            Double internationalShippingCost
    ) {
        if (processingTimeDays != null && processingTimeDays > 0) {
            this.processingTimeDays = processingTimeDays;
        }
        if (shippingTimeDays != null && shippingTimeDays > 0) {
            this.shippingTimeDays = shippingTimeDays;
        }
        if (freeShippingThreshold != null && freeShippingThreshold >= 0) {
            this.freeShippingThreshold = freeShippingThreshold;
        }
        if (domesticShippingCost != null && domesticShippingCost >= 0) {
            this.domesticShippingCost = domesticShippingCost;
        }
        if (internationalShippingCost != null && internationalShippingCost >= 0) {
            this.internationalShippingCost = internationalShippingCost;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAutoAcceptOrders(boolean autoAcceptOrders) {
        this.autoAcceptOrders = autoAcceptOrders;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAutoConfirmDelivery(boolean autoConfirmDelivery) {
        this.autoConfirmDelivery = autoConfirmDelivery;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBusinessHours(BusinessHours businessHours) {
        this.businessHours = businessHours;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateOrderStats(Double orderAmount, boolean isCancelled, boolean isRefunded) {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
        this.totalSales = (this.totalSales == null ? 0.0 : this.totalSales) + orderAmount;

        if (isCancelled) {
            this.canceledOrders = (this.canceledOrders == null ? 0 : this.canceledOrders) + 1;
        }

        if (isRefunded) {
            this.totalRefunds = (this.totalRefunds == null ? 0.0 : this.totalRefunds) + orderAmount;
        }

        this.averageOrderValue = this.totalOrders > 0 ?
                this.totalSales / this.totalOrders : 0.0;

        this.updatedAt = LocalDateTime.now();
    }

    public void updateProductStats(Integer totalProducts, Integer totalActiveProducts, Integer totalOutOfStockProducts) {
        if (totalProducts != null) {
            this.totalProducts = totalProducts;
        }
        if (totalActiveProducts != null) {
            this.totalActiveProducts = totalActiveProducts;
        }
        if (totalOutOfStockProducts != null) {
            this.totalOutOfStockProducts = totalOutOfStockProducts;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRating(Integer newRating) {
        int total = this.totalReviews == null ? 0 : this.totalReviews;
        double currentTotal = this.averageRating == null ? 0.0 : this.averageRating * total;

        this.totalReviews = total + 1;
        this.averageRating = (currentTotal + newRating) / this.totalReviews;
        this.updatedAt = LocalDateTime.now();

        if (newRating >= 4) {
            this.totalPositiveReviews = (this.totalPositiveReviews == null ? 0 : this.totalPositiveReviews) + 1;
        } else if (newRating == 3) {
            this.totalNeutralReviews = (this.totalNeutralReviews == null ? 0 : this.totalNeutralReviews) + 1;
        } else {
            this.totalNegativeReviews = (this.totalNegativeReviews == null ? 0 : this.totalNegativeReviews) + 1;
        }
    }

    public void updateFollowersCount(Integer increment) {
        this.followersCount = (this.followersCount == null ? 0 : this.followersCount) + increment;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateResponseRate(Integer totalResponses, Integer totalReviews) {
        if (totalReviews > 0) {
            this.responseRate = (double) totalResponses / totalReviews * 100;
        } else {
            this.responseRate = 100.0;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void updateResponseTimeHours(Double hours) {
        this.responseTimeHours = hours;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEarnings(Double amount) {
        this.totalEarnings = (this.totalEarnings == null ? 0.0 : this.totalEarnings) + amount;
        this.netEarnings = (this.netEarnings == null ? 0.0 : this.netEarnings) + amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCommissionPaid(Double commission) {
        this.totalCommissionPaid = (this.totalCommissionPaid == null ? 0.0 : this.totalCommissionPaid) + commission;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canSell() {
        return this.accountStatus == AccountStatus.ACTIVE &&
                this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED &&
                this.emailVerified;
    }

    public boolean canAddProducts() {
        return canSell() &&
                this.businessDetails != null &&
                this.bankDetails != null &&
                this.pickupAddress != null;
    }

    public boolean canAcceptOrders() {
        return canSell() && autoAcceptOrders;
    }

    public boolean canReceivePayouts() {
        return canPerformOperations() &&
                bankDetails != null &&
                bankDetails.accountNumber() != null &&
                bankDetails.accountHolderName() != null;
    }

    public boolean canPerformOperations() {
        return isActive() && isFullyVerified() && emailVerified;
    }

    public boolean canUpdateCommission() {
        return isActive() && isFullyVerified();
    }

    public boolean needsManualDeliveryConfirmation() {
        return !autoConfirmDelivery;
    }

    public boolean isPendingVerification() {
        return this.verificationStatus == SellerVerificationStatus.PENDING ||
                this.verificationStatus == SellerVerificationStatus.EMAIL_VERIFIED ||
                this.verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED;
    }

    public boolean isFullyVerified() {
        return this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED;
    }

    public boolean isActive() {
        return this.accountStatus == AccountStatus.ACTIVE;
    }

    public boolean isBanned() {
        return this.accountStatus == AccountStatus.BANNED;
    }

    public boolean isSuspended() {
        return this.accountStatus == AccountStatus.SUSPENDED;
    }

    public boolean hasReviews() {
        return totalReviews != null && totalReviews > 0;
    }

    public boolean hasSoldProducts() {
        return totalOrders != null && totalOrders > 0;
    }

    public boolean needsAdditionalVerification() {
        return verificationStatus == SellerVerificationStatus.BUSINESS_VERIFIED ||
                verificationStatus == SellerVerificationStatus.EMAIL_VERIFIED;
    }

    public String getStatusDisplayName() {
        if (accountStatus.isBlocked()) {
            return accountStatus.getDisplayName();
        }
        if (accountStatus.isPending()) {
            return "Verification Required";
        }
        if (!emailVerified) {
            return "Email Not Verified";
        }
        if (verificationStatus != SellerVerificationStatus.FULLY_VERIFIED) {
            return "Business Verification Required";
        }
        return accountStatus.getDisplayName();
    }

    public String getFullStoreName() {
        return storeName != null ? storeName : sellerName;
    }

    public String getContactEmail() {
        return storeEmail != null ? storeEmail : email.toString();
    }

    public String getContactPhone() {
        return storePhone != null ? storePhone : phoneNumber;
    }

    public String getRatingDisplay() {
        return averageRating != null ? String.format("%.1f", averageRating) : "N/A";
    }

    public SellerLevel getLevel() {
        if (totalOrders == null) return SellerLevel.BRONZE;

        if (totalOrders >= 1000) return SellerLevel.PLATINUM;
        if (totalOrders >= 500) return SellerLevel.GOLD;
        if (totalOrders >= 100) return SellerLevel.SILVER;
        return SellerLevel.BRONZE;
    }

    public Double getTotalEarnings() {
        return totalEarnings != null ? totalEarnings : 0.0;
    }

    public Double getTotalSales() {
        return totalSales != null ? totalSales : 0.0;
    }

    public Double getNetEarnings() {
        return netEarnings != null ? netEarnings : 0.0;
    }

    public Double getAverageOrderValue() {
        return averageOrderValue != null ? averageOrderValue : 0.0;
    }

    public void verifyTax(String verifiedBy) {
        this.taxInfoVerified = true;
        this.taxInfoVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStoreDetails(String storeName, String storeDescription, String storeTagline,
                                   String storeLogo, String storeBanner, String storeWebsite,
                                   String storeEmail, String storePhone) {
        updateStore(storeName, storeDescription, storeTagline, storeLogo, storeBanner, storeWebsite, storeEmail, storePhone);
    }

    public void updateSocialLinks(String socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTaxDetails(String gstNumber, String panNumber, String tinNumber, String businessRegistrationNumber) {
        updateTaxInfo(gstNumber, panNumber, tinNumber, businessRegistrationNumber);
    }

    public void addWarehouse(Address warehouseAddress) {
        addWarehouseAddress(warehouseAddress);
    }

    public void removeWarehouse(Address warehouseAddress) {
        removeWarehouseAddress(warehouseAddress);
    }

    public void updateCommission(double newRate, String updatedBy) {
        updateCommissionRate(newRate, updatedBy);
    }

    public void updateCashback(Double cashbackRate) {
        updateCashbackRate(cashbackRate);
    }

    public void updateShipping(Integer processingTimeDays, Integer shippingTimeDays,
                               Double freeShippingThreshold, Double domesticShippingCost, Double internationalShippingCost) {
        updateShippingSettings(processingTimeDays, shippingTimeDays, freeShippingThreshold, domesticShippingCost, internationalShippingCost);
    }

    public static Seller createPending(Email email, String sellerName, String mobile,
                                       BusinessDetails businessDetails, BankDetails bankDetails, Address pickupAddress) {
        return register(email, sellerName, mobile, businessDetails, bankDetails, pickupAddress);
    }

    public void verifyBusinessDetails(String verifiedBy, String notes) {
        if (this.verificationStatus == SellerVerificationStatus.FULLY_VERIFIED) {
            throw new DomainException("Business already fully verified");
        }
        this.verificationStatus = SellerVerificationStatus.BUSINESS_VERIFIED;
        this.businessVerifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void changeFollowers(Integer increment) {
        int current = this.followersCount == null ? 0 : this.followersCount;
        this.followersCount = Math.max(0, current + increment);
        this.updatedAt = LocalDateTime.now();
    }

    public void appendRating(Integer newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new DomainException("Rating must be between 1 and 5");
        }
        updateRating(newRating);
    }

    public void updateProductCounters(Integer totalProducts, Integer totalActiveProducts, Integer totalOutOfStockProducts) {
        updateProductStats(totalProducts, totalActiveProducts, totalOutOfStockProducts);
    }

    public void recalculateOrderStats(Double orderAmount, boolean isCancelled, boolean isRefunded) {
        this.totalOrders = (this.totalOrders == null ? 0 : this.totalOrders) + 1;
        this.totalSales = (this.totalSales == null ? 0.0 : this.totalSales) + orderAmount;
        this.totalTransactions = (this.totalTransactions == null ? 0 : this.totalTransactions) + 1;

        if (isCancelled) {
            this.canceledOrders = (this.canceledOrders == null ? 0 : this.canceledOrders) + 1;
        }
        if (isRefunded) {
            this.totalRefunds = (this.totalRefunds == null ? 0.0 : this.totalRefunds) + orderAmount;
        }

        this.cancellationRate = (double) this.canceledOrders / this.totalOrders * 100;
        this.averageOrderValue = this.totalSales / this.totalOrders;

        double commissionPaid = (this.totalCommissionPaid == null ? 0.0 : this.totalCommissionPaid);
        this.netEarnings = this.totalSales - (this.totalRefunds == null ? 0.0 : this.totalRefunds) - commissionPaid;

        this.updatedAt = LocalDateTime.now();
    }

    private void triggerUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Seller{sellerId=%s, email=%s, sellerName=%s, storeName=%s, accountStatus=%s, verificationStatus=%s}",
                sellerId, email, sellerName, storeName, accountStatus, verificationStatus);
    }
}