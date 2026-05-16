package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.domain.auth.model.VerificationCode;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.Otp;
import com.mygitgor.auth_service.infrastrucrure.persistance.entity.VerificationCodeEntity;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeMapper {

    public VerificationCodeEntity toEntity(VerificationCode domain) {
        if (domain == null) return null;

        return VerificationCodeEntity.builder()
                .id(domain.getId())
                .otp(domain.getOtp().getValue())
                .email(domain.getEmail().toString())
                .userRole(domain.getUserRole().name())
                .purpose(domain.getPurpose().name())
                .createdAt(domain.getCreatedAt())
                .expiresAt(domain.getOtp().getExpiresAt())
                .used(domain.isUsed())
                .build();
    }

    public VerificationCode toDomain(VerificationCodeEntity entity) {
        if (entity == null) return null;
        int validityMinutes = (int) java.time.Duration.between(entity.getCreatedAt(), entity.getExpiresAt()).toMinutes();
        Otp otp = new Otp(entity.getOtp(), (int) validityMinutes);

        return VerificationCode.builder()
                .id(entity.getId())
                .otp(otp)
                .email(new Email(entity.getEmail()))
                .userRole(UserRole.valueOf(entity.getUserRole()))
                .purpose(OtpPurpose.valueOf(entity.getPurpose()))
                .createdAt(entity.getCreatedAt())
                .used(entity.isUsed())
                .build();
    }
}
