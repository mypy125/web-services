package com.mygitgor.seller_service.application.dto.request;

import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.UserId;
import lombok.Builder;

@Builder
public record CreateRefundCommand(
        UserId customerId,
        OrderId orderId,
        Double amount,
        String reason
) {}
