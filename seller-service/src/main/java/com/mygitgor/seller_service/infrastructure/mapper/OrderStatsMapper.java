package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.external.OrderDetailsDto;
import com.mygitgor.seller_service.application.dto.external.ProductDetailsDto;
import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.domain.model.OrderStats;
import com.mygitgor.seller_service.domain.model.status.TransactionStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {TransactionStatus.class})
public interface OrderStatsMapper {

    @Mapping(target = "status", source = "status")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "quantity", constant = "0")
    @Mapping(target = "productPrice", ignore = true)
    @Mapping(target = "productTotal", ignore = true)
    @Mapping(target = "isNewCustomer", constant = "false")
    @Mapping(target = "isFirstPurchase", constant = "false")
    @Mapping(target = "tax", source = "tax", qualifiedByName = "nullToZero")
    @Mapping(target = "shippingCost", source = "shippingCost", qualifiedByName = "nullToZero")
    @Mapping(target = "discount", source = "discount", qualifiedByName = "nullToZero")
    @Mapping(target = "commission", source = "commission", qualifiedByName = "nullToZero")
    OrderStats toOrderStats(TransactionDto transaction);

    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "customerId", source = "order.customerId")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "category", source = "product.category")
    @Mapping(target = "quantity", source = "product.quantity")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "productTotal", source = "product.total")
    @Mapping(target = "isNewCustomer", source = "order.isNewCustomer")
    @Mapping(target = "isFirstPurchase", source = "order.isFirstPurchase")
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "tax", source = "transaction.tax", qualifiedByName = "nullToZero")
    @Mapping(target = "shippingCost", source = "transaction.shippingCost", qualifiedByName = "nullToZero")
    @Mapping(target = "discount", source = "transaction.discount", qualifiedByName = "nullToZero")
    @Mapping(target = "commission", source = "transaction.commission", qualifiedByName = "nullToZero")
    OrderStats toOrderStatsFull(TransactionDto transaction, OrderDetailsDto order, ProductDetailsDto product);


    @Named("statusToString")
    default String statusToString(TransactionStatus status) {
        if (status == null) {
            return "PENDING";
        }
        return status.name();
    }

    @Named("nullToZero")
    default Double nullToZero(Double value) {
        return value != null ? value : 0.0;
    }
}