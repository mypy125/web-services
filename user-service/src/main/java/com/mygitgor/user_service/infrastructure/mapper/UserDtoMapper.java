package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.dto.request.CreateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserStatisticsResponse;
import com.mygitgor.user_service.infrastructure.kafka.event.EmailVerifiedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserStatusChangedEvent;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserDtoMapper {

    public User toDomain(CreateUserRequest request) {
        return User.register(
                new Email(request.getEmail()),
                request.getFullName(),
                request.getPhoneNumber(),
                request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.ROLE_CUSTOMER
        );
    }

    public void updateDomain(User user, UpdateUserRequest request) {
        user.updateProfile(
                request.getFullName(),
                request.getProfileImage(),
                request.getPhoneNumber()
        );

        if (request.getDefaultAddressId() != null) {

        }

        if (request.getDefaultPaymentMethodId() != null) {

        }
    }

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus().name())
                .defaultAddressId(user.getDefaultAddressId())
                .defaultPaymentMethodId(user.getDefaultPaymentMethodId())
                .totalOrdersCount(user.getTotalOrdersCount())
                .totalSpentAmount(user.getTotalSpentAmount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .build();
    }

    public UserAuthInfoResponse toAuthInfoResponse(User user) {
        if (user == null) return null;

        return UserAuthInfoResponse.builder()
                .id(user.getId().toString())
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .accountStatus(user.getAccountStatus().name())
                .build();
    }

    public UserCreatedEvent toUserCreatedEvent(User user) {
        return UserCreatedEvent.builder()
                .userId(user.getId().toString())
                .email(user.getEmail().toString())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public EmailVerifiedEvent toEmailVerifiedEvent(User user) {
        return EmailVerifiedEvent.builder()
                .userId(user.getId().toString())
                .email(user.getEmail().toString())
                .verifiedAt(user.getEmailVerifiedAt())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public UserStatusChangedEvent toUserStatusChangedEvent(User user, String oldStatus, String reason, String changedBy) {
        return UserStatusChangedEvent.builder()
                .userId(user.getId().toString())
                .email(user.getEmail().toString())
                .oldStatus(oldStatus)
                .newStatus(user.getAccountStatus().name())
                .reason(reason)
                .changedBy(changedBy)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public UserStatisticsResponse toStatisticsResponse(UserId userId, String email, Object statistics) {
        return UserStatisticsResponse.builder()
                .userId(userId.toString())
                .email(email)
                .build();
    }
}
