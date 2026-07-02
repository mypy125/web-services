package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.external.AddressDto;
import com.mygitgor.seller_service.application.dto.external.BankDetailsDto;
import com.mygitgor.seller_service.application.dto.external.BusinessDetailsDto;
import com.mygitgor.seller_service.application.dto.request.AddressRequest;
import com.mygitgor.seller_service.application.dto.request.RegisterSellerRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateSellerRequest;
import com.mygitgor.seller_service.application.dto.response.*;
import com.mygitgor.seller_service.domain.model.Seller;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.BankDetails;
import com.mygitgor.seller_service.shared.valueobject.BusinessDetails;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.page.Page;
import com.mygitgor.seller_service.infrastructure.kafka.event.*;
import org.mapstruct.Mapper;
import org.mapstruct.*;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SellerMapper {

    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "warehouseAddresses", ignore = true)
    @Mapping(target = "storeCategories", ignore = true)
    @Mapping(target = "businessHours", ignore = true)
    @Mapping(target = "verificationDocument", ignore = true)
    Seller toDomain(RegisterSellerRequest request);

    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateDomain(@MappingTarget Seller seller, UpdateSellerRequest request);

    @Mapping(target = "canSell", expression = "java(seller.canSell())")
    @Mapping(target = "canAddProducts", expression = "java(seller.canAddProducts())")
    @Mapping(target = "canAcceptOrders", expression = "java(seller.canAcceptOrders())")
    @Mapping(target = "canReceivePayouts", expression = "java(seller.canReceivePayouts())")
    @Mapping(target = "isFullyVerified", expression = "java(seller.isFullyVerified())")
    @Mapping(target = "isActive", expression = "java(seller.isActive())")
    @Mapping(target = "isBanned", expression = "java(seller.isBanned())")
    @Mapping(target = "isSuspended", expression = "java(seller.isSuspended())")
    @Mapping(target = "statusDisplayName", expression = "java(seller.getStatusDisplayName())")
    @Mapping(target = "fullStoreName", expression = "java(seller.getFullStoreName())")
    @Mapping(target = "contactEmail", expression = "java(seller.getContactEmail())")
    @Mapping(target = "contactPhone", expression = "java(seller.getContactPhone())")
    @Mapping(target = "ratingDisplay", expression = "java(seller.getRatingDisplay())")
    SellerResponse toResponse(Seller seller);

    @Mapping(target = "fullStoreName", expression = "java(seller.getFullStoreName())")
    @Mapping(target = "ratingDisplay", expression = "java(seller.getRatingDisplay())")
    SellerRegistrationResponse toRegistrationResponse(Seller seller);

    @Mapping(target = "canSell", expression = "java(seller.canSell())")
    @Mapping(target = "canAddProducts", expression = "java(seller.canAddProducts())")
    @Mapping(target = "fullStoreName", expression = "java(seller.getFullStoreName())")
    @Mapping(target = "ratingDisplay", expression = "java(seller.getRatingDisplay())")
    SellerProfileResponse toProfileResponse(Seller seller);

    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    SellerRegisteredEvent toRegisteredEvent(Seller seller);

    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    SellerActivatedEvent toActivatedEvent(Seller seller, String activatedBy);

    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    SellerSuspendedEvent toSuspendedEvent(Seller seller, String reason, String suspendedBy);

    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    SellerBannedEvent toBannedEvent(Seller seller, String reason, String bannedBy);

    @Mapping(target = "verifiedAt", source = "emailVerifiedAt")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    EmailVerifiedEvent toEmailVerifiedEvent(Seller seller);

    @Mapping(target = "verifiedAt", source = "businessVerifiedAt")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    BusinessVerifiedEvent toBusinessVerifiedEvent(Seller seller, String verifiedBy);

    BusinessDetails toBusinessDetails(BusinessDetailsDto dto);
    BankDetails toBankDetails(BankDetailsDto dto);
    Address toAddress(AddressDto dto);

    BusinessDetailsDto toBusinessDetailsDto(BusinessDetails domain);
    BankDetailsDto toBankDetailsDto(BankDetails domain);

    @Mapping(target = "sellerId", source = "sellerId")
    AddressResponse toAddressResponse(Address domain);

    Address toAddress(AddressRequest request);
    AddressDto toAddressDto(Address domain);

    @Mapping(target = "id", source = "sellerId")
    @Mapping(target = "fullName", source = "sellerName")
    @Mapping(target = "role", constant = "ROLE_SELLER")
    @Mapping(target = "emailVerified", expression = "java(seller.isEmailVerified())")
    UserAuthInfoResponse toUserAuthInfoResponse(Seller seller);


    default Page<SellerResponse> toResponsePage(Page<Seller> sellerPage) {
        return sellerPage == null ? Page.empty() : sellerPage.map(this::toResponse);
    }

    default Page<SellerProfileResponse> toProfilePage(Page<Seller> sellerPage) {
        return sellerPage == null ? Page.empty() : sellerPage.map(this::toProfileResponse);
    }

    List<SellerResponse> toResponseList(List<Seller> sellers);

    default String mapSellerIdToString(SellerId id) {
        return id == null ? null : id.toString();
    }

    default SellerId mapStringToSellerId(String id) {
        return (id == null || id.isBlank()) ? null : new SellerId(java.util.UUID.fromString(id));
    }

    default String mapEmailToString(Email email) {
        return email == null ? null : email.value();
    }

    default Email mapStringToEmail(String email) {
        return (email == null || email.isBlank()) ? null : new Email(email);
    }
}