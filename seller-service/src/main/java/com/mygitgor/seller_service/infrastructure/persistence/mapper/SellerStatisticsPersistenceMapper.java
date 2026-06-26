package com.mygitgor.seller_service.infrastructure.persistence.mapper;

import com.mygitgor.seller_service.domain.model.statistic.SellerStatistics;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.StatisticsId;
import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerStatisticsEntity;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.InjectionStrategy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface SellerStatisticsPersistenceMapper {
    @Mapping(target = "id", source = "id", qualifiedByName = "mapStatisticsIdToUuid")
    @Mapping(target = "totalSellers", source = "totalSellers", defaultValue = "0L")
    @Mapping(target = "activeSellers", source = "activeSellers", defaultValue = "0L")
    @Mapping(target = "suspendedSellers", source = "suspendedSellers", defaultValue = "0L")
    @Mapping(target = "bannedSellers", source = "bannedSellers", defaultValue = "0L")
    @Mapping(target = "pendingVerification", source = "pendingVerification", defaultValue = "0L")
    @Mapping(target = "fullyVerified", source = "fullyVerified", defaultValue = "0L")
    @Mapping(target = "rejected", source = "rejected", defaultValue = "0L")
    @Mapping(target = "averageRating", source = "averageRating", defaultValue = "0.0")
    @Mapping(target = "averageOrderValue", source = "averageOrderValue", defaultValue = "0.0")
    @Mapping(target = "averageCommissionRate", source = "averageCommissionRate", defaultValue = "0.0")
    @Mapping(target = "averageResponseRate", source = "averageResponseRate", defaultValue = "0.0")
    @Mapping(target = "averageResponseTimeHours", source = "averageResponseTimeHours", defaultValue = "0.0")
    @Mapping(target = "totalEarnings", source = "totalEarnings", defaultValue = "0.0")
    @Mapping(target = "totalSales", source = "totalSales", defaultValue = "0.0")
    @Mapping(target = "totalCommissionPaid", source = "totalCommissionPaid", defaultValue = "0.0")
    @Mapping(target = "totalOrders", source = "totalOrders", defaultValue = "0")
    @Mapping(target = "totalProducts", source = "totalProducts", defaultValue = "0")
    @Mapping(target = "calculatedAt", source = "calculatedAt", defaultExpression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    SellerStatisticsEntity toEntity(SellerStatistics domain);

    @Mapping(target = "id", source = "id", qualifiedByName = "mapUuidToStatisticsId")
    SellerStatistics toDomain(SellerStatisticsEntity entity);

    @Named("mapStatisticsIdToUuid")
    default UUID mapStatisticsIdToUuid(StatisticsId id) {
        return (id != null && id.getValue() != null) ? id.getValue() : UUID.randomUUID();
    }

    @Named("mapUuidToStatisticsId")
    default StatisticsId mapUuidToStatisticsId(UUID id) {
        return id != null ? new StatisticsId(id) : new StatisticsId();
    }
}