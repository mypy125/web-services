package com.mygitgor.auth_service.application.mapper;

import com.mygitgor.auth_service.application.dto.response.AuthResponseDto;
import com.mygitgor.auth_service.application.dto.response.UserInfoResponseDto;
import com.mygitgor.auth_service.domain.auth.model.Token;
import com.mygitgor.auth_service.domain.auth.model.aggregate.AuthAggregate;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import jakarta.validation.constraints.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
    AuthResponseDto toAuthResponseDto(AuthAggregate aggregate);

    @Mapping(source = "token.value.value", target = "token")
    @Mapping(source = "token.expiresAt", target = "expiresAt")
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    AuthResponseDto toAuthResponseDto(Token token);

    @Mapping(source = "email.value", target = "email")
    @Mapping(source = "userId.value", target = "userId")
    UserInfoResponseDto toUserInfoResponseDto(Email email, UserId userId, AuthAggregate aggregate);
}
