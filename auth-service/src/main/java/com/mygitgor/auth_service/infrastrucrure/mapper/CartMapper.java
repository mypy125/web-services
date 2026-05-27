package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.domain.cart.model.Cart;
import com.mygitgor.auth_service.domain.cart.model.CartAnalytics;
import com.mygitgor.auth_service.domain.cart.model.CartItem;
import com.mygitgor.auth_service.domain.cart.model.CartSummary;
import com.mygitgor.auth_service.domain.cart.model.CartValidationResult;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {UserId.class})
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    @Mapping(target = "userId", source = "userId", qualifiedByName = "toUserId")
    @Mapping(target = "items", source = "items", qualifiedByName = "toCartItemList")
    Cart toDomain(CartDto dto);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sellerId", source = "sellerId")
    CartItem toDomain(CartItemDto dto);

    @Mapping(target = "cartId", source = "cartId")
    @Mapping(target = "itemSummaries", source = "itemSummaries", qualifiedByName = "toItemSummaryList")
    CartSummary toDomain(CartSummaryDto dto);

    @Mapping(target = "productId", source = "productId")
    CartSummary.CartItemSummary toDomain(CartItemSummaryDto dto);

    @Mapping(target = "valid", source = "valid")
    @Mapping(target = "unavailableItems", source = "unavailableItems", qualifiedByName = "toCartItemList")
    @Mapping(target = "priceChangedItems", source = "priceChangedItems", qualifiedByName = "toCartItemList")
    CartValidationResult toDomain(CartValidationResultDto dto);

    @Mapping(target = "totalActiveCarts", source = "totalActiveCarts")
    @Mapping(target = "totalAbandonedCarts", source = "totalAbandonedCarts")
    @Mapping(target = "averageCartValue", source = "averageCartValue")
    @Mapping(target = "conversionRate", source = "conversionRate")
    CartAnalytics toDomain(CartAnalyticsDto dto);

    @Mapping(target = "userId", source = "userId", qualifiedByName = "fromUserId")
    @Mapping(target = "items", source = "items", qualifiedByName = "toCartItemDtoList")
    CartDto toDto(Cart domain);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "sellerId", source = "sellerId")
    CartItemDto toDto(CartItem domain);

    @Mapping(target = "cartId", source = "cartId")
    @Mapping(target = "itemSummaries", source = "itemSummaries", qualifiedByName = "toItemSummaryDtoList")
    CartSummaryDto toDto(CartSummary domain);

    @Mapping(target = "productId", source = "productId")
    CartItemSummaryDto toDto(CartSummary.CartItemSummary domain);

    @Mapping(target = "valid", source = "valid")
    @Mapping(target = "unavailableItems", source = "unavailableItems", qualifiedByName = "toCartItemDtoList")
    @Mapping(target = "priceChangedItems", source = "priceChangedItems", qualifiedByName = "toCartItemDtoList")
    CartValidationResultDto toDto(CartValidationResult domain);

    @Mapping(target = "totalActiveCarts", source = "totalActiveCarts")
    @Mapping(target = "totalAbandonedCarts", source = "totalAbandonedCarts")
    CartAnalyticsDto toDto(CartAnalytics domain);


    @Named("toUserId")
    default UserId toUserId(String userId) {
        if (userId == null) return null;
        return new UserId(userId);
    }

    @Named("fromUserId")
    default String fromUserId(UserId userId) {
        if (userId == null) return null;
        return userId.toString();
    }

    @Named("toCartItemList")
    default List<CartItem> toCartItemList(List<CartItemDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Named("toCartItemDtoList")
    default List<CartItemDto> toCartItemDtoList(List<CartItem> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Named("toItemSummaryList")
    default List<CartSummary.CartItemSummary> toItemSummaryList(List<CartItemSummaryDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Named("toItemSummaryDtoList")
    default List<CartItemSummaryDto> toItemSummaryDtoList(List<CartSummary.CartItemSummary> items) {
        if (items == null) return null;
        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
