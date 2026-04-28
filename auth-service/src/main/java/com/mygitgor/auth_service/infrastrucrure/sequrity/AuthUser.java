package com.mygitgor.auth_service.infrastrucrure.sequrity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser implements Serializable {
    private String email;
    private String userId;
    private String role;

    public boolean isCustomer() {
        return "ROLE_CUSTOMER".equals(role);
    }

    public boolean isSeller() {
        return "ROLE_SELLER".equals(role);
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(role);
    }
}
