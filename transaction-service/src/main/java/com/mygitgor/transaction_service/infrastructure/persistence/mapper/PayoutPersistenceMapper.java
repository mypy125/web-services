package com.mygitgor.transaction_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.transaction_service.domain.model.Payout;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.PayoutType;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.PayoutEntity;
import com.mygitgor.transaction_service.shared.valueobject.PayoutId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, PayoutType.class, PayoutStatus.class})
public interface PayoutPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "payoutId", qualifiedByName = "payoutIdToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToUuid")
    @Mapping(target = "settlementId", source = "settlementId", qualifiedByName = "settlementIdToUuid")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "transactionIdsJson", source = "transactionIds", qualifiedByName = "setToJson")
    @Mapping(target = "metadataJson", source = "metadata", qualifiedByName = "mapToJson")
    PayoutEntity toEntity(Payout payout);

    @Mapping(target = "payoutId", source = "id", qualifiedByName = "uuidToPayoutId")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToSellerId")
    @Mapping(target = "settlementId", source = "settlementId", qualifiedByName = "uuidToSettlementId")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "transactionIds", source = "transactionIdsJson", qualifiedByName = "jsonToSet")
    @Mapping(target = "metadata", source = "metadataJson", qualifiedByName = "jsonToMap")
    Payout toDomain(PayoutEntity entity);

    @Named("payoutIdToUuid")
    default UUID payoutIdToUuid(PayoutId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToPayoutId")
    default PayoutId uuidToPayoutId(UUID uuid) {
        return uuid != null ? new PayoutId(uuid) : null;
    }

    @Named("sellerIdToUuid")
    default UUID sellerIdToUuid(SellerId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToSellerId")
    default SellerId uuidToSellerId(UUID uuid) {
        return uuid != null ? new SellerId(uuid) : null;
    }

    @Named("settlementIdToUuid")
    default UUID settlementIdToUuid(String settlementId) {
        return settlementId != null && !settlementId.isBlank() ? UUID.fromString(settlementId) : null;
    }

    @Named("uuidToSettlementId")
    default String uuidToSettlementId(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("typeToString")
    default String typeToString(PayoutType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToType")
    default PayoutType stringToType(String typeStr) {
        return typeStr != null ? PayoutType.valueOf(typeStr) : null;
    }

    @Named("statusToString")
    default String statusToString(PayoutStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default PayoutStatus stringToStatus(String statusStr) {
        return statusStr != null ? PayoutStatus.valueOf(statusStr) : null;
    }

    @Named("setToJson")
    default String setToJson(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(set);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToSet")
    default Set<String> jsonToSet(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashSet<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Set.class);
        } catch (Exception e) {
            return new LinkedHashSet<>();
        }
    }

    @Named("mapToJson")
    default String mapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToMap")
    default Map<String, String> jsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
