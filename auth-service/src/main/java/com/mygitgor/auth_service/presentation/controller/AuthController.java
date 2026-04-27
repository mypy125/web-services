package com.mygitgor.auth_service.presentation.controller;

import com.mygitgor.auth_service.application.command.LoginCommand;
import com.mygitgor.auth_service.application.command.RegisterCustomerCommand;
import com.mygitgor.auth_service.application.command.RegisterSellerCommand;
import com.mygitgor.auth_service.application.command.RequestOtpCommand;
import com.mygitgor.auth_service.application.dto.request.LoginRequestDto;
import com.mygitgor.auth_service.application.dto.request.OtpRequestDto;
import com.mygitgor.auth_service.application.dto.request.SignupRequestDto;
import com.mygitgor.auth_service.application.dto.request.seller.SellerRegistrationRequestDto;
import com.mygitgor.auth_service.application.dto.response.ApiResponse;
import com.mygitgor.auth_service.application.dto.response.AuthResponseDto;
import com.mygitgor.auth_service.application.service.AuthApplicationService;
import com.mygitgor.auth_service.application.service.OtpApplicationService;
import com.mygitgor.auth_service.domain.auth.model.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private final AuthApplicationService authService;
    private final OtpApplicationService otpService;

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP", description = "Request OTP for registration, login, or email verification")
    public ResponseEntity<ApiResponse> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        RequestOtpCommand command = RequestOtpCommand.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .purpose(OtpPurpose.valueOf(request.getPurpose()))
                .build();

        otpService.requestOtp(command);

        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with OTP", description = "Login using email and OTP")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginCommand command = LoginCommand.builder()
                .email(request.getEmail())
                .otp(request.getOtp())
                .build();

        AuthResponseDto response = authService.login(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/customer")
    @Operation(summary = "Register new customer", description = "Register a new customer account")
    public ResponseEntity<AuthResponseDto> registerCustomer(@Valid @RequestBody SignupRequestDto request) {
        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .otp(request.getOtp())
                .build();

        AuthResponseDto response = authService.registerCustomer(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/seller")
    @Operation(summary = "Register new seller", description = "Register a new seller account")
    public ResponseEntity<AuthResponseDto> registerSeller(@Valid @RequestBody SellerRegistrationRequestDto request) {
        RegisterSellerCommand command = RegisterSellerCommand.builder()
                .email(request.getEmail())
                .sellerName(request.getSellerName())
                .mobile(request.getMobile())
                .businessDetails(request.getBusinessDetails())
                .bankDetails(request.getBankDetails())
                .build();

        AuthResponseDto response = authService.registerSeller(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/seller/verify")
    @Operation(summary = "Verify seller email", description = "Verify seller email with OTP and complete registration")
    public ResponseEntity<AuthResponseDto> verifySeller(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        AuthResponseDto response = authService.verifySellerAndLogin(email, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout from current device")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String email
    ) {
        String token = extractToken(authHeader);
        authService.logout(token, email);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "Logout from all devices", description = "Logout from all active sessions")
    public ResponseEntity<ApiResponse> logoutAllDevices(@RequestParam String email) {
        authService.logoutAllDevices(email);
        return ResponseEntity.ok(ApiResponse.success("Logged out from all devices"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if JWT token is valid")
    public ResponseEntity<ApiResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        boolean isValid = authService.validateToken(token);

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Token is valid"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid token"));
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}
