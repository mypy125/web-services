package com.mygitgor.seller_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.domain.model.Transaction;
import com.mygitgor.seller_service.domain.model.status.TransactionStatus;
import com.mygitgor.seller_service.domain.model.type.TransactionType;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.TransactionId;
import com.mygitgor.seller_service.shared.valueobject.id.UserId;
import com.mygitgor.seller_service.infrastructure.persistence.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "transactionId.value")
    @Mapping(target = "sellerId", source = "sellerId.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "orderId", source = "orderId.value")
    @Mapping(target = "metadataJson", source = "metadata", qualifiedByName = "metadataToJson")
    TransactionEntity toEntity(Transaction domain);

    default Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;

        return Transaction.builder()
                .transactionId(new TransactionId(entity.id()))
                .sellerId(new SellerId(entity.sellerId()))
                .customerId(new UserId(entity.customerId()))
                .orderId(new OrderId(entity.orderId()))
                .type(entity.type() != null ? TransactionType.valueOf(entity.type()) : null)
                .status(entity.status() != null ? TransactionStatus.valueOf(entity.status()) : null)
                .amount(entity.amount())
                .tax(entity.tax())
                .commission(entity.commission())
                .shippingCost(entity.shippingCost())
                .discount(entity.discount())
                .netAmount(entity.netAmount())
                .currency(entity.currency())
                .description(entity.description())
                .referenceNumber(entity.referenceNumber())
                .paymentMethod(entity.paymentMethod())
                .paymentGateway(entity.paymentGateway())
                .bankReference(entity.bankReference())
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .completedAt(entity.completedAt())
                .refundedAt(entity.refundedAt())
                .notes(entity.notes())
                .processedBy(entity.processedBy())
                .ipAddress(entity.ipAddress())
                .userAgent(entity.userAgent())
                .metadata(jsonToMetadata(entity.metadataJson()))
                .build();
    }

    @Named("metadataToJson")
    default String metadataToJson(Map<String, String> map) {
        if (map == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting transaction metadata to JSON string", e);
        }
    }

    default Map<String, String> jsonToMetadata(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to transaction metadata", e);
        }
    }
}
