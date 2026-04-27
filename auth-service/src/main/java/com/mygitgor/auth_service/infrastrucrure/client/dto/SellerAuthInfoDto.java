package com.mygitgor.auth_service.infrastrucrure.client.dto;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAuthInfoDto {
    private String id;
    private String email;
    private String sellerName;
    private UserRole role;
    private boolean emailVerified;
}
