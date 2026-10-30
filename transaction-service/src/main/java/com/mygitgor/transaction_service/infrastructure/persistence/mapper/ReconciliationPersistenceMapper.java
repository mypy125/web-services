package com.mygitgor.transaction_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.transaction_service.domain.model.Reconciliation;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.ReconciliationType;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.ReconciliationEntity;
import com.mygitgor.transaction_service.shared.valueobject.ReconciliationId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.math.BigDecimal;
import java.util.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, ReconciliationType.class, ReconciliationStatus.class})
public interface ReconciliationPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "reconciliationId", qualifiedByName = "reconciliationIdToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToUuid")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "discrepanciesByTypeJson", source = "discrepanciesByType", qualifiedByName = "discrepanciesMapToJson")
    @Mapping(target = "resolutionActionsJson", source = "resolutionActions", qualifiedByName = "listToJson")
    @Mapping(target = "notesJson", source = "notes", qualifiedByName = "listToJson")
    @Mapping(target = "metadataJson", source = "metadata", qualifiedByName = "mapToJson")
    ReconciliationEntity toEntity(Reconciliation reconciliation);

    @Mapping(target = "reconciliationId", source = "id", qualifiedByName = "uuidToReconciliationId")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToSellerId")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "discrepanciesByType", source = "discrepanciesByTypeJson", qualifiedByName = "jsonToDiscrepanciesMap")
    @Mapping(target = "resolutionActions", source = "resolutionActionsJson", qualifiedByName = "jsonToList")
    @Mapping(target = "notes", source = "notesJson", qualifiedByName = "jsonToList")
    @Mapping(target = "metadata", source = "metadataJson", qualifiedByName = "jsonToMap")
    Reconciliation toDomain(ReconciliationEntity entity);


    @Named("reconciliationIdToUuid")
    default UUID reconciliationIdToUuid(ReconciliationId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToReconciliationId")
    default ReconciliationId uuidToReconciliationId(UUID uuid) {
        return uuid != null ? new ReconciliationId(uuid) : null;
    }

    @Named("sellerIdToUuid")
    default UUID sellerIdToUuid(SellerId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToSellerId")
    default SellerId uuidToSellerId(UUID uuid) {
        return uuid != null ? new SellerId(uuid) : null;
    }

    @Named("typeToString")
    default String typeToString(ReconciliationType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToType")
    default ReconciliationType stringToType(String typeStr) {
        return typeStr != null ? ReconciliationType.valueOf(typeStr) : null;
    }

    @Named("statusToString")
    default String statusToString(ReconciliationStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default ReconciliationStatus stringToStatus(String statusStr) {
        return statusStr != null ? ReconciliationStatus.valueOf(statusStr) : null;
    }

    @Named("discrepanciesMapToJson")
    default String discrepanciesMapToJson(Map<String, BigDecimal> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToDiscrepanciesMap")
    default Map<String, BigDecimal> jsonToDiscrepanciesMap(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
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
