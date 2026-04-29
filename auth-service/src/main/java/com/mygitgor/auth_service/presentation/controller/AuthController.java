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
import com.mygitgor.auth_service.application.mapper.CommandMapper;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {
    private final AuthApplicationService authService;
    private final OtpApplicationService otpService;
    private final CommandMapper commandMapper;

    @PostMapping("/otp/request")
    @Operation(summary = "Request OTP", description = "Request OTP for registration, login, or email verification")
    public Mono<ResponseEntity<ApiResponse>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        RequestOtpCommand command = RequestOtpCommand.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .purpose(OtpPurpose.valueOf(request.getPurpose()))
                .build();

        return otpService.requestOtp(command)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("OTP sent successfully")))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()))
                ));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with OTP", description = "Login using email and OTP")
    public Mono<ResponseEntity<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        LoginCommand command = commandMapper.toLoginCommand(request);

        return authService.login(command)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Login failed: {}", e.getMessage());
                    AuthResponseDto errorResponse = AuthResponseDto.builder()
                            .message("Login failed: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }

    @PostMapping("/register/customer")
    @Operation(summary = "Register new customer", description = "Register a new customer account")
    public Mono<ResponseEntity<AuthResponseDto>> registerCustomer(@Valid @RequestBody SignupRequestDto request) {
        RegisterCustomerCommand command = commandMapper.toRegisterCustomerCommand(request);

        return authService.registerCustomer(command)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Customer registration failed: {}", e.getMessage());
                    AuthResponseDto errorResponse = AuthResponseDto.builder()
                            .message("Registration failed: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    @PostMapping("/register/seller")
    @Operation(summary = "Register new seller", description = "Register a new seller account")
    public Mono<ResponseEntity<AuthResponseDto>> registerSeller(@Valid @RequestBody SellerRegistrationRequestDto request) {
        RegisterSellerCommand command = commandMapper.toRegisterSellerCommand(request);

        return authService.registerSeller(command)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(e -> {
                    log.error("Seller registration failed: {}", e.getMessage());
                    AuthResponseDto errorResponse = AuthResponseDto.builder()
                            .message("Seller registration failed: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    @PostMapping("/seller/verify")
    @Operation(summary = "Verify seller email", description = "Verify seller email with OTP and complete registration")
    public Mono<ResponseEntity<AuthResponseDto>> verifySeller(
            @RequestParam String email,
            @RequestParam String otp
    ) {
        return authService.verifySellerAndLogin(email, otp)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Seller verification failed: {}", e.getMessage());
                    AuthResponseDto errorResponse = AuthResponseDto.builder()
                            .message("Seller verification failed: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout from current device")
    public Mono<ResponseEntity<ApiResponse>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String email
    ) {
        String token = extractToken(authHeader);

        return authService.logout(token, email)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("Logout successful")))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()))
                ));
    }

    @PostMapping("/logout/all")
    @Operation(summary = "Logout from all devices", description = "Logout from all active sessions")
    public Mono<ResponseEntity<ApiResponse>> logoutAllDevices(@RequestParam String email) {
        return authService.logoutAllDevices(email)
                .thenReturn(ResponseEntity.ok(ApiResponse.success("Logged out from all devices")))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()))
                ));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Check if JWT token is valid")
    public Mono<ResponseEntity<ApiResponse>> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);

        return authService.validateToken(token)
                .map(isValid -> {
                    if (isValid) {
                        return ResponseEntity.ok(ApiResponse.success("Token is valid"));
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Invalid token"));
                    }
                })
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Token validation failed: " + e.getMessage()))
                ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh expired JWT token")
    public Mono<ResponseEntity<AuthResponseDto>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);

        return authService.refreshToken(token)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Token refresh failed: {}", e.getMessage());
                    AuthResponseDto errorResponse = AuthResponseDto.builder()
                            .message("Token refresh failed: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }

    @GetMapping("/user-info")
    @Operation(summary = "Get user info from token", description = "Get user information from JWT token")
    public Mono<ResponseEntity<ApiResponse>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);

        return authService.getUserInfoFromToken(token)
                .map(userInfo -> ResponseEntity.ok(ApiResponse.success("User info retrieved", userInfo)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(e.getMessage()))
                ));
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}