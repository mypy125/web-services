package com.mygitgor.auth_service.infrastrucrure.mapper;

import com.mygitgor.auth_service.domain.user.model.User;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.domain.user.model.UserStatistics;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserAuthInfoDto;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserDto;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserPageDto;
import com.mygitgor.auth_service.infrastrucrure.client.dto.UserStatisticsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", source = "id", qualifiedByName = "toUserId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    User toDomain(UserDto dto);

    @Mapping(target = "id", source = "id", qualifiedByName = "toUserId")
    @Mapping(target = "email", source = "email", qualifiedByName = "toEmail")
    User toDomain(UserAuthInfoDto dto);

    @Mapping(target = "userId", source = "userId")
    UserStatistics toDomain(UserStatisticsDto dto);

    @Mapping(target = "content", source = "content", qualifiedByName = "mapContent")
    @Mapping(target = "pageNumber", source = "pageNumber")
    @Mapping(target = "pageSize", source = "pageSize")
    @Mapping(target = "totalElements", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "last", source = "last")
    @Mapping(target = "first", source = "first")
    @Mapping(target = "numberOfElements", source = "numberOfElements")
    @Mapping(target = "empty", source = "empty")
    Page<User> toDomain(UserPageDto dto);

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

    @Named("mapContent")
    default List<User> mapContent(List<UserDto> content) {
        if (content == null) return List.of();
        return content.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}
