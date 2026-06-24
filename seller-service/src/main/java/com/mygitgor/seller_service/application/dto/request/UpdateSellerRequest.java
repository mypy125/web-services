package com.mygitgor.seller_service.application.dto.request;

public record UpdateSellerRequest(
        String sellerName,
        String displayName,
        String mobile,
        String phoneNumber,
        String profileImage,
        String coverImage
) {}
