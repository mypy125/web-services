package com.mygitgor.user_service.infrastructure.mapper;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.infrastructure.dto.request.CreateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.request.UpdateUserRequest;
import com.mygitgor.user_service.infrastructure.dto.response.UserAuthInfoResponse;
import com.mygitgor.user_service.infrastructure.dto.response.UserResponse;
import com.mygitgor.user_service.infrastructure.kafka.event.EmailVerifiedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserCreatedEvent;
import com.mygitgor.user_service.infrastructure.kafka.event.UserStatusChangedEvent;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDateTime.class, UserId.class, Email.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", expression = "java(new Email(request.getEmail()))")
    @Mapping(target = "role", expression = "java(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.ROLE_CUSTOMER)")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "accountStatus", constant = "ACTIVE")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    User toDomain(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "profileImage", source = "profileImage")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateDomain(@MappingTarget User user, UpdateUserRequest request);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "accountStatus", source = "accountStatus")
    UserResponse toResponse(User user);

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "accountStatus", source = "accountStatus")
    UserAuthInfoResponse toAuthInfoResponse(User user);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    UserCreatedEvent toUserCreatedEvent(User user);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "verifiedAt", source = "emailVerifiedAt")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    EmailVerifiedEvent toEmailVerifiedEvent(User user);

    @Mapping(target = "userId", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "oldStatus", source = "oldStatus")
    @Mapping(target = "newStatus", source = "user.accountStatus")
    @Mapping(target = "occurredAt", expression = "java(LocalDateTime.now())")
    UserStatusChangedEvent toUserStatusChangedEvent(User user, String oldStatus, String reason, String changedBy);

    default Page<UserResponse> toResponsePage(Page<User> userPage) {
        return userPage.map(this::toResponse);
    }

    default List<UserResponse> toResponseList(List<User> users) {
        return users.stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
