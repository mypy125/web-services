package com.mygitgor.seller_service.application.dto.request;

import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import lombok.Builder;

@Builder
public record CreateCommissionCommand(
        OrderId orderId,
        Double amount,
        String description
) {}
