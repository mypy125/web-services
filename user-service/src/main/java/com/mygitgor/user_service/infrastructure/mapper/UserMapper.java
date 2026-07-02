package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.application.dto.response.UserResponse;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.application.dto.external.UserProfileDto;
import com.mygitgor.user_service.application.dto.request.CreateUserRequest;
import com.mygitgor.user_service.application.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.kafka.event.EmailVerifiedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserStatusChangedEvent;
import com.mygitgor.user_service.shared.valueobject.Email;
import com.mygitgor.user_service.shared.valueobject.Page;
import com.mygitgor.user_service.shared.valueobject.UserId;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDateTime.class, UserId.class, Email.class, UserRole.class})
public interface UserMapper {

    default User toDomain(CreateUserRequest request) {
        if (request == null) return null;

        UserRole targetRole = request.role() != null ? UserRole.valueOf(request.role()) : UserRole.ROLE_CUSTOMER;

        return User.register(
                new Email(request.email()),
                request.fullName(),
                request.phoneNumber(),
                targetRole
        );
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "fullName", source = "request.fullName")
    @Mapping(target = "phoneNumber", source = "request.phoneNumber")
    @Mapping(target = "profileImage", source = "request.profileImage")
    @Mapping(target = "defaultAddressId", source = "user.defaultAddressId")
    @Mapping(target = "defaultPaymentMethodId", source = "user.defaultPaymentMethodId")
    @Mapping(target = "defaultShippingAddressId", source = "user.defaultShippingAddressId")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    User updateDomain(User user, UpdateUserRequest request);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    UserResponse toResponse(User user);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    UserAuthInfoResponse toAuthInfoResponse(User user);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "canLogin", expression = "java(user.canLogin())")
    @Mapping(target = "canPurchase", expression = "java(user.canPurchase())")
    @Mapping(target = "needsEmailVerification", expression = "java(user.needsEmailVerification())")
    @Mapping(target = "locked", expression = "java(user.isLocked())")
    @Mapping(target = "displayName", expression = "java(user.getFullName() != null ? user.getFullName() : user.getEmail().value())")
    @Mapping(target = "defaultAddress", ignore = true)
    @Mapping(target = "defaultShippingAddress", ignore = true)
    @Mapping(target = "defaultPaymentMethod", ignore = true)
    @Mapping(target = "recentOrders", ignore = true)
    @Mapping(target = "currentCart", ignore = true)
    @Mapping(target = "loyaltyPoints", ignore = true)
    @Mapping(target = "loyaltyTier", ignore = true)
    UserProfileDto toProfileDto(User user);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    UserCreatedEvent toUserCreatedEvent(User user);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "verifiedAt", source = "emailVerifiedAt")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    EmailVerifiedEvent toEmailVerifiedEvent(User user);

    @Mapping(target = "userId", source = "user.id.value")
    @Mapping(target = "email", source = "user.email.value")
    @Mapping(target = "newStatus", source = "user.accountStatus")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    UserStatusChangedEvent toUserStatusChangedEvent(User user, String oldStatus, String reason, String changedBy);

    default Page<UserResponse> toResponsePage(Page<User> userPage) {
        if (userPage == null) return null;
        return userPage.map(this::toResponse);
    }

    default List<UserResponse> toResponseList(List<User> users) {
        if (users == null) return null;
        return users.stream().map(this::toResponse).toList();
    }

    default Page<UserProfileDto> toProfilePage(Page<User> userPage) {
        if (userPage == null) return null;
        return userPage.map(this::toProfileDto);
    }

    default List<UserProfileDto> toProfileList(List<User> users) {
        if (users == null) return null;
        return users.stream().map(this::toProfileDto).toList();
    }
}