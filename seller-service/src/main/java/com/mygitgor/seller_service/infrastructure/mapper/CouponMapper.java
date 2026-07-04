package com.mygitgor.seller_service.infrastructure.mapper;

import com.mygitgor.seller_service.application.dto.response.CouponResponse;
import com.mygitgor.seller_service.domain.model.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "mapIdToString")
    @Mapping(target = "sellerId", source = "sellerId", qualifiedByName = "mapIdToString")
    @Mapping(target = "type", expression = "java(coupon.getType() != null ? coupon.getType().name() : null)")
    @Mapping(target = "discountType", expression = "java(coupon.getDiscountType() != null ? coupon.getDiscountType().name() : null)")
    @Mapping(target = "valid", expression = "java(coupon.isValidNow())")
    @Mapping(target = "expired", expression = "java(coupon.getValidUntil() != null && coupon.getValidUntil().isBefore(LocalDateTime.now()))")
    @Mapping(target = "fullyUsed", expression = "java(coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit())")
    @Mapping(target = "discountDisplay", ignore = true)
    @Mapping(target = "daysUntilExpiration", ignore = true)
    @Mapping(target = "remainingUses", ignore = true)
    CouponResponse toResponse(Coupon coupon);

    @Named("mapIdToString")
    default String mapIdToString(Object id) {
        return id != null ? id.toString() : null;
    }
}