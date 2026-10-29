package com.mygitgor.transaction_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.transaction_service.domain.model.Settlement;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.SettlementType;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.SettlementEntity;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.SettlementId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, SettlementType.class, SettlementStatus.class})
public interface SettlementPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "settlementId", qualifiedByName = "settlementIdToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToUuid")
    @Mapping(target = "reconciliationId", source = "reconciliationId", qualifiedByName = "reconciliationIdToUuid")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "transactionIdsJson", source = "transactionIds", qualifiedByName = "setToJson")
    @Mapping(target = "payoutIdsJson", source = "payoutIds", qualifiedByName = "setToJson")
    @Mapping(target = "refundIdsJson", source = "refundIds", qualifiedByName = "setToJson")
    @Mapping(target = "notesJson", source = "notes", qualifiedByName = "listToJson")
    @Mapping(target = "metadataJson", source = "metadata", qualifiedByName = "mapToJson")
    SettlementEntity toEntity(Settlement settlement);

    @Mapping(target = "settlementId", source = "id", qualifiedByName = "uuidToSettlementId")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToSellerId")
    @Mapping(target = "reconciliationId", source = "reconciliationId", qualifiedByName = "uuidToReconciliationId")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "transactionIds", source = "transactionIdsJson", qualifiedByName = "jsonToSet")
    @Mapping(target = "payoutIds", source = "payoutIdsJson", qualifiedByName = "jsonToSet")
    @Mapping(target = "refundIds", source = "refundIdsJson", qualifiedByName = "jsonToSet")
    @Mapping(target = "notes", source = "notesJson", qualifiedByName = "jsonToList")
    @Mapping(target = "metadata", source = "metadataJson", qualifiedByName = "jsonToMap")
    Settlement toDomain(SettlementEntity entity);

    @Named("settlementIdToUuid")
    default UUID settlementIdToUuid(SettlementId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToSettlementId")
    default SettlementId uuidToSettlementId(UUID uuid) {
        return uuid != null ? new SettlementId(uuid) : null;
    }

    @Named("sellerIdToUuid")
    default UUID sellerIdToUuid(SellerId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToSellerId")
    default SellerId uuidToSellerId(UUID uuid) {
        return uuid != null ? new SellerId(uuid) : null;
    }

    @Named("reconciliationIdToUuid")
    default UUID reconciliationIdToUuid(String reconciliationId) {
        return reconciliationId != null && !reconciliationId.isBlank() ? UUID.fromString(reconciliationId) : null;
    }

    @Named("uuidToReconciliationId")
    default String uuidToReconciliationId(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("typeToString")
    default String typeToString(SettlementType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToType")
    default SettlementType stringToType(String typeStr) {
        return typeStr != null ? SettlementType.valueOf(typeStr) : null;
    }

    @Named("statusToString")
    default String statusToString(SettlementStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default SettlementStatus stringToStatus(String statusStr) {
        return statusStr != null ? SettlementStatus.valueOf(statusStr) : null;
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

    @Named("listToJson")
    default String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
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
