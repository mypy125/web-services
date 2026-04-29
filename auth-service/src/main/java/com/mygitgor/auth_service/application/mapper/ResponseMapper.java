package com.mygitgor.auth_service.application.mapper;

import com.mygitgor.auth_service.application.dto.response.AuthResponseDto;
import com.mygitgor.auth_service.application.dto.response.UserInfoResponseDto;
import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.aggregate.AuthAggregate;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResponseMapper {

    @Mapping(source = "aggregate.currentToken.value.value", target = "token")
    @Mapping(source = "aggregate.email.value", target = "email")
    @Mapping(source = "aggregate.userId.value", target = "userId")
    @Mapping(source = "aggregate.role", target = "role")
    @Mapping(source = "aggregate.currentToken.expiresAt", target = "expiresAt")
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "requiresEmailVerification", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    AuthResponseDto toAuthResponseDto(AuthAggregate aggregate);

    @Mapping(source = "token.value.value", target = "token")
    @Mapping(source = "token.email.value", target = "email")
    @Mapping(source = "token.userId.value", target = "userId")
    @Mapping(source = "token.role", target = "role")
    @Mapping(source = "token.expiresAt", target = "expiresAt")
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "requiresEmailVerification", ignore = true)
    @Mapping(target = "accountStatus", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    AuthResponseDto toAuthResponseDto(Token token);

    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "userId.value", target = "userId")
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    UserInfoResponseDto toUserInfoResponseDto(Email email, UserId userId, AuthAggregate aggregate);
}
