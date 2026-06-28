package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.response.TransactionResponse;
import com.mygitgor.seller_service.shared.valueobject.OrderStats;
import com.mygitgor.seller_service.domain.model.Transaction;
import com.mygitgor.seller_service.shared.valueobject.TransactionStatus;
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

    default OrderStats toOrderStats(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return OrderStats.builder()
                .amount(transaction.getAmount())
                .tax(transaction.getTax() != null ? transaction.getTax() : 0.0)
                .shippingCost(transaction.getShippingCost() != null ? transaction.getShippingCost() : 0.0)
                .discount(transaction.getDiscount() != null ? transaction.getDiscount() : 0.0)
                .commission(transaction.getCommission() != null ? transaction.getCommission() : 0.0)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : TransactionStatus.PENDING.name())
                .isNewCustomer(false)
                .customerId(transaction.getCustomerId() != null ? transaction.getCustomerId().toString() : null)
                .productId(null)
                .productName(null)
                .category(null)
                .quantity(0)
                .productPrice(null)
                .productTotal(null)
                .isFirstPurchase(false)
                .build();
    }
}
