package com.mygitgor.user_service.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDto {
    private String id;
    private String userId;
    private String type;
    private String last4Digits;
    private String cardType;
    private String expiryMonth;
    private String expiryYear;
    private boolean isDefault;
}
