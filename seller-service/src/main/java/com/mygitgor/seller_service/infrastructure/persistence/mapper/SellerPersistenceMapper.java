package com.mygitgor.seller_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mygitgor.seller_service.domain.model.*;
import com.mygitgor.seller_service.domain.model.status.AccountStatus;
import com.mygitgor.seller_service.domain.model.status.SellerVerificationStatus;
import com.mygitgor.seller_service.shared.valueobject.*;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SellerPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mapping(target = "id", source = "sellerId.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "businessDetailsJson", source = "businessDetails", qualifiedByName = "toJson")
    @Mapping(target = "bankDetailsJson", source = "bankDetails", qualifiedByName = "toJson")
    @Mapping(target = "pickupAddressJson", source = "pickupAddress", qualifiedByName = "toJson")
    @Mapping(target = "returnAddressJson", source = "returnAddress", qualifiedByName = "toJson")
    @Mapping(target = "warehouseAddressesJson", source = "warehouseAddresses", qualifiedByName = "toJson")
    @Mapping(target = "verificationDocumentJson", source = "verificationDocument", qualifiedByName = "toJson")
    @Mapping(target = "businessHoursJson", source = "businessHours", qualifiedByName = "toJson")
    @Mapping(target = "storeCategoriesJson", source = "storeCategories", qualifiedByName = "toJson")
    @Mapping(target = "verificationStatus", source = "verificationStatus")
    @Mapping(target = "accountStatus", source = "accountStatus")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "storeCategory", source = "storeCategory")
    SellerEntity toEntity(Seller domain);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "sellerId", source = "sellerId")
    AddressEntity toEntity(Address domain, UUID sellerId);

    Address toDomain(AddressEntity entity);

    default Seller toDomain(SellerEntity entity) {
        if (entity == null) return null;

        return Seller.builder()
                .sellerId(new SellerId(entity.id()))
                .email(new Email(entity.email()))
                .sellerName(entity.sellerName())
                .storeName(entity.storeName())
                .displayName(entity.displayName())
                .mobile(entity.mobile())
                .phoneNumber(entity.phoneNumber())
                .profileImage(entity.profileImage())
                .coverImage(entity.coverImage())
                .businessDetails(fromJson(entity.businessDetailsJson(), BusinessDetails.class))
                .bankDetails(fromJson(entity.bankDetailsJson(), BankDetails.class))
                .pickupAddress(fromJson(entity.pickupAddressJson(), Address.class))
                .returnAddress(fromJson(entity.returnAddressJson(), Address.class))
                .warehouseAddresses(fromJsonList(entity.warehouseAddressesJson(), new TypeReference<List<Address>>() {}))
                .verificationDocument(fromJson(entity.verificationDocumentJson(), VerificationDocument.class))
                .businessHours(fromJson(entity.businessHoursJson(), BusinessHours.class))
                .storeCategories(fromJsonList(entity.storeCategoriesJson(), new TypeReference<List<StoreCategory>>() {}))
                .gstNumber(entity.gstNumber())
                .panNumber(entity.panNumber())
                .tinNumber(entity.tinNumber())
                .businessRegistrationNumber(entity.businessRegistrationNumber())
                .taxInfoVerified(entity.taxInfoVerified())
                .taxInfoVerifiedAt(entity.taxInfoVerifiedAt())
                .emailVerified(entity.emailVerified())
                .verificationStatus(entity.verificationStatus() != null ? SellerVerificationStatus.valueOf(entity.verificationStatus()) : null)
                .accountStatus(entity.accountStatus() != null ? AccountStatus.valueOf(entity.accountStatus()) : null)
                .rejectionReason(entity.rejectionReason())
                .rejectedAt(entity.rejectedAt())
                .role(entity.role() != null ? UserRole.valueOf(entity.role()) : null)
                .commissionRate(entity.commissionRate())
                .minimumCommissionRate(entity.minimumCommissionRate())
                .maximumCommissionRate(entity.maximumCommissionRate())
                .cashbackRate(entity.cashbackRate())
                .autoAcceptOrders(entity.autoAcceptOrders())
                .autoConfirmDelivery(entity.autoConfirmDelivery())
                .processingTimeDays(entity.processingTimeDays())
                .shippingTimeDays(entity.shippingTimeDays())
                .freeShippingThreshold(entity.freeShippingThreshold())
                .domesticShippingCost(entity.domesticShippingCost())
                .internationalShippingCost(entity.internationalShippingCost())
                .storeLogo(entity.storeLogo())
                .storeBanner(entity.storeBanner())
                .storeDescription(entity.storeDescription())
                .storeTagline(entity.storeTagline())
                .storeWebsite(entity.storeWebsite())
                .storeEmail(entity.storeEmail())
                .storePhone(entity.storePhone())
                .socialMediaLinks(entity.socialMediaLinks())
                .storeCategory(entity.storeCategory() != null ? StoreCategory.valueOf(entity.storeCategory()) : null)
                .averageRating(entity.averageRating())
                .totalReviews(entity.totalReviews())
                .totalPositiveReviews(entity.totalPositiveReviews())
                .totalNeutralReviews(entity.totalNeutralReviews())
                .totalNegativeReviews(entity.totalNegativeReviews())
                .responseRate(entity.responseRate())
                .responseTimeHours(entity.responseTimeHours())
                .followersCount(entity.followersCount())
                .totalOrders(entity.totalOrders())
                .totalProducts(entity.totalProducts())
                .totalActiveProducts(entity.totalActiveProducts())
                .totalOutOfStockProducts(entity.totalOutOfStockProducts())
                .totalEarnings(entity.totalEarnings())
                .totalSales(entity.totalSales())
                .totalRefunds(entity.totalRefunds())
                .totalTax(entity.totalTax())
                .netEarnings(entity.netEarnings())
                .averageOrderValue(entity.averageOrderValue())
                .conversionRate(entity.conversionRate())
                .returnRate(entity.returnRate())
                .cancellationRate(entity.cancellationRate())
                .canceledOrders(entity.canceledOrders())
                .totalTransactions(entity.totalTransactions())
                .totalCommissionPaid(entity.totalCommissionPaid())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .lastLoginAt(entity.lastLoginAt())
                .emailVerifiedAt(entity.emailVerifiedAt())
                .businessVerifiedAt(entity.businessVerifiedAt())
                .lastActiveAt(entity.lastActiveAt())
                .commissionUpdatedAt(entity.commissionUpdatedAt())
                .suspendedAt(entity.suspendedAt())
                .bannedAt(entity.bannedAt())
                .reactivatedAt(entity.reactivatedAt())
                .build();
    }

    @Named("toJson")
    default String toJson(Object object) {
        if (object == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON string", e);
        }
    }

    default <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to object", e);
        }
    }

    default <T> List<T> fromJsonList(String json, TypeReference<List<T>> typeReference) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to list", e);
        }
    }
}
