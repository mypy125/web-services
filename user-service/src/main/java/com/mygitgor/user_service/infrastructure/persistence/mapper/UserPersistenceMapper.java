package com.mygitgor.user_service.infrastructure.persistence.mapper;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserEntity;
import com.mygitgor.user_service.shared.valueobject.Email;
import com.mygitgor.user_service.shared.valueobject.UserId;

import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    UserEntity toEntity(User domain);

    default User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return User.builder()
                .id(new UserId(entity.id()))
                .email(new Email(entity.email()))
                .fullName(entity.fullName())
                .role(UserRole.valueOf(entity.role()))
                .emailVerified(entity.emailVerified())
                .profileImage(entity.profileImage())
                .phoneNumber(entity.phoneNumber())
                .accountStatus(AccountStatus.valueOf(entity.accountStatus()))
                .createdAt(entity.createdAt())
                .updatedAt(entity.updatedAt())
                .lastLoginAt(entity.lastLoginAt())
                .emailVerifiedAt(entity.emailVerifiedAt())
                .defaultAddressId(entity.defaultAddressId())
                .defaultPaymentMethodId(entity.defaultPaymentMethodId())
                .defaultShippingAddressId(entity.defaultShippingAddressId())
                .totalOrdersCount(entity.totalOrdersCount())
                .totalSpentAmount(entity.totalSpentAmount())
                .language(entity.language())
                .timezone(entity.timezone())
                .newsletterSubscribed(entity.newsletterSubscribed())
                .marketingConsent(entity.marketingConsent())
                .lastPasswordChangeAt(entity.lastPasswordChangeAt())
                .failedLoginAttempts(entity.failedLoginAttempts())
                .lockedUntil(entity.lockedUntil())
                .build();
    }
}
