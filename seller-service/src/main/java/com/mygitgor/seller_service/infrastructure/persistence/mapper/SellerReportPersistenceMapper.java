package com.mygitgor.seller_service.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.shared.valueobject.ReportPeriod;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerReportEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SellerReportPersistenceMapper {
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mapping(target = "id", source = "reportId.value")
    @Mapping(target = "sellerId", source = "sellerId.value")
    @Mapping(target = "period", source = "period")
    @Mapping(target = "comparisonMetricsJson", source = "comparisonMetrics", qualifiedByName = "mapToJson")
    SellerReportEntity toEntity(SellerReport domain);

    default SellerReport toDomain(SellerReportEntity entity) {
        if (entity == null) return null;

        return SellerReport.builder()
                .reportId(new SellerReportId(entity.id()))
                .sellerId(new SellerId(entity.sellerId()))
                .period(entity.period() != null ? ReportPeriod.valueOf(entity.period()) : null)
                .periodStart(entity.periodStart())
                .periodEnd(entity.periodEnd())
                .totalEarnings(entity.totalEarnings())
                .totalSales(entity.totalSales())
                .totalRefunds(entity.totalRefunds())
                .totalTax(entity.totalTax())
                .netEarnings(entity.netEarnings())
                .totalCommission(entity.totalCommission())
                .totalShippingCost(entity.totalShippingCost())
                .totalDiscountGiven(entity.totalDiscountGiven())
                .totalCashbackGiven(entity.totalCashbackGiven())
                .totalOrders(entity.totalOrders())
                .completedOrders(entity.completedOrders())
                .canceledOrders(entity.canceledOrders())
                .returnedOrders(entity.returnedOrders())
                .refundedOrders(entity.refundedOrders())
                .pendingOrders(entity.pendingOrders())
                .processingOrders(entity.processingOrders())
                .shippedOrders(entity.shippedOrders())
                .deliveredOrders(entity.deliveredOrders())
                .totalTransactions(entity.totalTransactions())
                .totalProductsSold(entity.totalProductsSold())
                .totalUniqueProductsSold(entity.totalUniqueProductsSold())
                .bestSellingProductId(entity.bestSellingProductId())
                .bestSellingProductName(entity.bestSellingProductName())
                .bestSellingProductQuantity(entity.bestSellingProductQuantity())
                .bestSellingProductRevenue(entity.bestSellingProductRevenue())
                .topCategory(entity.topCategory())
                .topCategorySales(entity.topCategorySales())
                .totalCustomers(entity.totalCustomers())
                .newCustomers(entity.newCustomers())
                .returningCustomers(entity.returningCustomers())
                .customerRetentionRate(entity.customerRetentionRate())
                .averageOrderValue(entity.averageOrderValue())
                .averageCustomerLifetimeValue(entity.averageCustomerLifetimeValue())
                .averageRating(entity.averageRating())
                .totalReviews(entity.totalReviews())
                .positiveReviews(entity.positiveReviews())
                .neutralReviews(entity.neutralReviews())
                .negativeReviews(entity.negativeReviews())
                .responseRate(entity.responseRate())
                .averageResponseTimeHours(entity.averageResponseTimeHours())
                .conversionRate(entity.conversionRate())
                .returnRate(entity.returnRate())
                .cancellationRate(entity.cancellationRate())
                .refundRate(entity.refundRate())
                .fulfillmentRate(entity.fulfillmentRate())
                .onTimeDeliveryRate(entity.onTimeDeliveryRate())
                .profitMargin(entity.profitMargin())
                .growthPercentage(entity.growthPercentage())
                .comparisonMetrics(jsonToMap(entity.comparisonMetricsJson()))
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .reportGeneratedAt(entity.reportGeneratedAt())
                .build();
    }

    @Named("mapToJson")
    default String mapToJson(Map<String, Double> map) {
        if (map == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting comparison metrics map to JSON string", e);
        }
    }

    default Map<String, Double> jsonToMap(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON string to comparison metrics map", e);
        }
    }
}
