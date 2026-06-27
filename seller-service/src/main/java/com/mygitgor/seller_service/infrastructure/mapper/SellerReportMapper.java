package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.response.SellerReportResponse;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.domain.model.shared.valueobject.id.SellerReportId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SellerReportMapper {

    @Mapping(target = "reportId", source = "reportId", qualifiedByName = "reportIdToString")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "sellerIdToString")
    @Mapping(target = "bestSellingProduct", expression = "java(report.getBestSellingProductName() + \" (\" + report.getBestSellingProductQuantity() + \" шт.)\")")
    @Mapping(target = "profitMarginDisplay", expression = "java(String.format(\"%.2f%%\", report.getProfitMargin()))")
    @Mapping(target = "growthPercentageDisplay", expression = "java(report.getGrowthPercentage() != null ? String.format(\"%.2f%%\", report.getGrowthPercentage()) : \"0%\")")
    SellerReportResponse toResponse(SellerReport report);

    @Named("reportIdToString")
    default String reportIdToString(SellerReportId reportId) {
        return reportId != null ? reportId.toString() : null;
    }

    @Named("sellerIdToString")
    default String sellerIdToString(SellerId sellerId) {
        return sellerId != null ? sellerId.toString() : null;
    }
}
