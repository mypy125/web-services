package com.mygitgor.user_service.infrastructure.persistence.mapper;

import com.mygitgor.user_service.domain.model.AccountStatus;
import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import com.mygitgor.user_service.infrastructure.persistence.entity.UserEntity;
import com.mygitgor.user_service.infrastructure.shared.valueobject.Email;
import com.mygitgor.user_service.infrastructure.shared.valueobject.UserId;

import java.util.UUID;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPersistenceMapper {

    UserEntity toEntity(User domain);

    User toDomain(UserEntity entity);

    default UUID mapUserIdToUuid(UserId id) {
        if (id == null || id.toString() == null) return null;
        return UUID.fromString(id.toString());
    }

    default UserId mapUuidToUserId(UUID uuid) {
        if (uuid == null) return null;
        return new UserId(uuid.toString());
    }

    default String mapEmailToString(Email email) {
        return email != null ? email.toString() : null;
    }

    default Email mapStringToEmail(String emailStr) {
        return emailStr != null ? new Email(emailStr) : null;
    }


    default String mapRoleToString(UserRole role) {
        return role != null ? role.name() : null;
    }

    default UserRole mapStringToUserRole(String roleStr) {
        return roleStr != null ? UserRole.valueOf(roleStr) : null;
    }

    default String mapStatusToString(AccountStatus status) {
        return status != null ? status.name() : null;
    }

    default AccountStatus mapStringToAccountStatus(String statusStr) {
        return statusStr != null ? AccountStatus.valueOf(statusStr) : null;
    }
}
