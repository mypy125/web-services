package com.mygitgor.auth_service.application.mapper;

import com.mygitgor.auth_service.application.command.LoginCommand;
import com.mygitgor.auth_service.application.command.RegisterCustomerCommand;
import com.mygitgor.auth_service.application.command.RegisterSellerCommand;
import com.mygitgor.auth_service.application.dto.request.LoginRequestDto;
import com.mygitgor.auth_service.application.dto.request.OtpRequestDto;
import com.mygitgor.auth_service.application.dto.request.SignupRequestDto;
import com.mygitgor.auth_service.application.dto.request.seller.SellerRegistrationRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommandMapper {

    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(target = "userAgent", ignore = true)
    LoginCommand toLoginCommand(LoginRequestDto dto);

    @Mapping(target = "ipAddress", ignore = true)
    RegisterCustomerCommand toRegisterCustomerCommand(SignupRequestDto dto);

    @Mapping(target = "ipAddress", ignore = true)
    @Mapping(source = "businessDetails", target = "businessDetails")
    @Mapping(source = "bankDetails", target = "bankDetails")
    @Mapping(source = "pickupAddress", target = "pickupAddress")
    RegisterSellerCommand toRegisterSellerCommand(SellerRegistrationRequestDto dto);
}
