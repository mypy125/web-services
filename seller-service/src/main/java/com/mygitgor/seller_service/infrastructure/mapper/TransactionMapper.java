package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.response.TransactionResponse;
import com.mygitgor.seller_service.domain.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "transactionId.value")
    @Mapping(target = "sellerId", source = "sellerId.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "orderId", source = "orderId.value")
    @Mapping(target = "financialBreakdown.amount", source = "amount")
    @Mapping(target = "financialBreakdown.tax", source = "tax")
    @Mapping(target = "financialBreakdown.commission", source = "commission")
    @Mapping(target = "financialBreakdown.shippingCost", source = "shippingCost")
    @Mapping(target = "financialBreakdown.discount", source = "discount")
    @Mapping(target = "financialBreakdown.netAmount", source = "netAmount")
    @Mapping(target = "paymentDetails.paymentMethod", source = "paymentMethod")
    @Mapping(target = "paymentDetails.paymentGateway", source = "paymentGateway")
    @Mapping(target = "paymentDetails.bankReference", source = "bankReference")
    @Mapping(target = "audit.notes", source = "notes")
    @Mapping(target = "audit.processedBy", source = "processedBy")
    @Mapping(target = "audit.ipAddress", source = "ipAddress")
    @Mapping(target = "audit.userAgent", source = "userAgent")
    @Mapping(target = "audit.metadata", source = "metadata")
    TransactionResponse toResponse(Transaction transaction);
}
