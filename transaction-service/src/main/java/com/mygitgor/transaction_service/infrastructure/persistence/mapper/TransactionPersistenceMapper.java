package com.mygitgor.transaction_service.infrastructure.persistence.mapper;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.transaction_service.domain.model.Transaction;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionStatus;
import com.mygitgor.transaction_service.domain.model.valueobject.TransactionType;
import com.mygitgor.transaction_service.infrastructure.persistence.entity.TransactionEntity;
import com.mygitgor.transaction_service.shared.valueobject.OrderId;
import com.mygitgor.transaction_service.shared.valueobject.SellerId;
import com.mygitgor.transaction_service.shared.valueobject.TransactionId;
import com.mygitgor.transaction_service.shared.valueobject.UserId;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UUID.class, TransactionType.class, TransactionStatus.class})
public interface TransactionPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "transactionId", qualifiedByName = "transactionIdToUuid")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToUuid")
    @Mapping(target = "customerId", source = "customerId", qualifiedByName = "customerIdToUuid")
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "orderIdToUuid")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    @Mapping(target = "metadataJson", source = "metadata", qualifiedByName = "mapToJson")
    TransactionEntity toEntity(Transaction transaction);

    @Mapping(target = "transactionId", source = "id", qualifiedByName = "uuidToTransactionId")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "uuidToSellerId")
    @Mapping(target = "customerId", source = "customerId", qualifiedByName = "uuidToCustomerId")
    @Mapping(target = "orderId", source = "orderId", qualifiedByName = "uuidToOrderId")
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    @Mapping(target = "status", source = "status", qualifiedByName = "stringToStatus")
    @Mapping(target = "metadata", source = "metadataJson", qualifiedByName = "jsonToMap")
    Transaction toDomain(TransactionEntity entity);

    @Named("transactionIdToUuid")
    default UUID transactionIdToUuid(TransactionId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToTransactionId")
    default TransactionId uuidToTransactionId(UUID uuid) {
        return uuid != null ? new TransactionId(uuid) : null;
    }

    @Named("sellerIdToUuid")
    default UUID sellerIdToUuid(SellerId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToSellerId")
    default SellerId uuidToSellerId(UUID uuid) {
        return uuid != null ? new SellerId(uuid) : null;
    }

    @Named("customerIdToUuid")
    default UUID customerIdToUuid(UserId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToCustomerId")
    default UserId uuidToCustomerId(UUID uuid) {
        return uuid != null ? new UserId(uuid) : null;
    }

    @Named("orderIdToUuid")
    default UUID orderIdToUuid(OrderId id) {
        return id != null ? UUID.fromString(id.toString()) : null;
    }

    @Named("uuidToOrderId")
    default OrderId uuidToOrderId(UUID uuid) {
        return uuid != null ? new OrderId(uuid) : null;
    }

    @Named("typeToString")
    default String typeToString(TransactionType type) {
        return type != null ? type.name() : null;
    }

    @Named("stringToType")
    default TransactionType stringToType(String typeStr) {
        return typeStr != null ? TransactionType.valueOf(typeStr) : null;
    }

    @Named("statusToString")
    default String statusToString(TransactionStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default TransactionStatus stringToStatus(String statusStr) {
        return statusStr != null ? TransactionStatus.valueOf(statusStr) : null;
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
