package com.mygitgor.seller_service.application.dto.request;

import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.UserId;
import lombok.Builder;

@Builder
public record CreateSaleCommand(
        UserId customerId,
        OrderId orderId,
        Double amount,
        Double tax,
        Double commission,
        Double shippingCost,
        Double discount
) {}
