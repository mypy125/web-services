package com.mygitgor.seller_service.infrastructure.persistence.mapper;

import com.mygitgor.seller_service.domain.model.type.AddressType;
import com.mygitgor.seller_service.infrastructure.persistence.entity.AddressEntity;
import com.mygitgor.seller_service.shared.valueobject.Address;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, AddressType.class})
public interface AddressPersistenceMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "stringIdToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToUuid")
    @Mapping(target = "addressType", source = "addressType", qualifiedByName = "addressTypeToString")
    AddressEntity toEntity(Address address);

    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToStringId")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToSellerId")
    @Mapping(target = "addressType", source = "addressType", qualifiedByName = "stringToAddressType")
    Address toDomain(AddressEntity entity);

    @Named("stringIdToUuid")
    default UUID stringIdToUuid(String id) {
        return id != null && !id.isBlank() ? UUID.fromString(id) : null;
    }

    @Named("uuidToStringId")
    default String uuidToStringId(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("sellerIdToUuid")
    default UUID sellerIdToUuid(SellerId sellerId) {
        return sellerId != null ? UUID.fromString(sellerId.toString()) : null;
    }

    @Named("uuidToSellerId")
    default SellerId uuidToSellerId(UUID uuid) {
        return uuid != null ? new SellerId(uuid) : null;
    }

    @Named("addressTypeToString")
    default String addressTypeToString(AddressType addressType) {
        return addressType != null ? addressType.name() : null;
    }

    @Named("stringToAddressType")
    default AddressType stringToAddressType(String addressTypeStr) {
        return addressTypeStr != null ? AddressType.valueOf(addressTypeStr) : null;
    }
}
