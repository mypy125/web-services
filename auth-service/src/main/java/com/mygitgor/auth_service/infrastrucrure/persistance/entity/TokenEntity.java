package com.mygitgor.auth_service.infrastrucrure.persistance.entity;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
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
@Table("tokens")
public class TokenEntity {
    @Id
    private UUID id;
    private String value;
    private String email;
    private String userId;
    private String role;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String status;
    private LocalDateTime blacklistedAt;
    private String blacklistReason;

    public static TokenEntity fromDomain(Token token) {
        return TokenEntity.builder()
                .id(UUID.randomUUID())
                .value(token.getValue().toString())
                .email(token.getEmail().toString())
                .userId(token.getUserId().toString())
                .role(token.getRole().name())
                .issuedAt(token.getIssuedAt())
                .expiresAt(token.getExpiresAt())
                .status(token.getStatus().name())
                .blacklistedAt(token.getStatus() == TokenStatus.BLACKLISTED ? LocalDateTime.now() : null)
                .blacklistReason(null)
                .build();
    }

    public Token toDomain() {
        return Token.builder()
                .value(new TokenValue(this.value))
                .email(new Email(this.email))
                .userId(new UserId(this.userId))
                .role(UserRole.valueOf(this.role))
                .issuedAt(this.issuedAt)
                .expiresAt(this.expiresAt)
                .status(TokenStatus.valueOf(this.status))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return "ACTIVE".equals(status) && !isExpired();
    }
}
