package com.mygitgor.auth_service.infrastrucrure.persistance.entity;


import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("verification_codes")
public class VerificationCodeEntity {
    @Id
    private UUID id;
    private String otp;
    private String email;
    private String userRole;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used;

    public VerificationCodeEntity(String otp, String email, String userRole,
                                  String purpose, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.otp = otp;
        this.email = email;
        this.userRole = userRole;
        this.purpose = purpose;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
