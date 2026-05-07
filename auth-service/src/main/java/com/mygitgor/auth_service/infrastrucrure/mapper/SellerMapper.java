package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import com.mygitgor.auth_service.domain.seller.model.Seller;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.SellerAuthInfoDto;
import com.mygitgor.auth_service.infrastrucrure.client.dto.SellerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring", imports = {SellerId.class, Email.class, UserId.class})
public interface SellerMapper {

    SellerMapper INSTANCE = Mappers.getMapper(SellerMapper.class);

    @Mapping(target = "sellerId", source = "id", qualifiedByName = "toSellerId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "toUserId")
    @Mapping(target = "businessDetails", source = "businessDetails")
    @Mapping(target = "bankDetails", source = "bankDetails")
    @Mapping(target = "pickupAddress", source = "pickupAddress")
    @Mapping(target = "rejectionReason", ignore = true)
    Seller toDomain(SellerDto dto);

    @Mapping(target = "sellerId", source = "id", qualifiedByName = "toSellerId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "toUserId")
    Seller toDomain(SellerAuthInfoDto dto);

    @Mapping(target = "businessAddress", source = "businessAddress")
    @Mapping(target = "businessEmail", source = "businessEmail", qualifiedByName = "toEmail")
    BusinessDetails toBusinessDetails(BusinessDetailsDto dto);

    BankDetails toBankDetails(BankDetailsDto dto);

    Address toAddress(AddressDto dto);

    @Named("toSellerId")
    default SellerId toSellerId(UUID uuid) {
        if (uuid == null) return null;
        return new SellerId(uuid.toString());
    }

    @Named("toSellerIdFromString")
    default SellerId toSellerIdFromString(String id) {
        if (id == null) return null;
        return new SellerId(id);
    }

    @Named("toEmail")
    default Email toEmail(String email) {
        if (email == null) return null;
        return new Email(email);
    }

    @Named("toUserId")
    default UserId toUserId(String userId) {
        if (userId == null) return null;
        return new UserId(userId);
    }

    @Named("fromSellerId")
    default String fromSellerId(SellerId sellerId) {
        if (sellerId == null) return null;
        return sellerId.toString();
    }

    @Named("fromEmail")
    default String fromEmail(Email email) {
        if (email == null) return null;
        return email.toString();
    }

    @Named("fromUserId")
    default String fromUserId(UserId userId) {
        if (userId == null) return null;
        return userId.toString();
    }
}
