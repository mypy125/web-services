package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserAuthInfoDto;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "toUserId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    User toDomain(UserDto dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "toUserId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    User toDomain(UserAuthInfoDto dto);

    @Named("toUserId")
    default UserId toUserId(String id) {
        if (id == null) return null;
        return new UserId(id);
    }

    @Named("toEmail")
    default Email toEmail(String email) {
        if (email == null) return null;
        return new Email(email);
    }

    @Named("fromUserId")
    default String fromUserId(UserId userId) {
        if (userId == null) return null;
        return userId.toString();
    }

    @Named("fromEmail")
    default String fromEmail(Email email) {
        if (email == null) return null;
        return email.toString();
    }
}
