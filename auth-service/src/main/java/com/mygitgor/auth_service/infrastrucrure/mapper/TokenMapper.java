package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.enums.TokenStatus;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.TokenValue;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.TokenEntity;
import org.springframework.stereotype.Component;

@Component
public class TokenMapper {

    public TokenEntity toEntity(Token domain) {
        if (domain == null) return null;

        return TokenEntity.builder()
                .id(null)
                .value(domain.getValue().toString())
                .email(domain.getEmail().toString())
                .userId(domain.getUserId().toString())
                .role(domain.getRole().name())
                .issuedAt(domain.getIssuedAt())
                .expiresAt(domain.getExpiresAt())
                .status(domain.getStatus().name())
                .build();
    }

    public Token toDomain(TokenEntity entity) {
        if (entity == null) return null;

        return Token.builder()
                .value(new TokenValue(entity.getValue()))
                .email(new Email(entity.getEmail()))
                .userId(new UserId(entity.getUserId()))
                .role(UserRole.valueOf(entity.getRole()))
                .issuedAt(entity.getIssuedAt())
                .expiresAt(entity.getExpiresAt())
                .status(TokenStatus.valueOf(entity.getStatus()))
                .build();
    }
}
