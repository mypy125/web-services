package com.mygitgor.seller_service.presentation.internal;

import com.mygitgor.seller_service.application.dto.request.RegisterSellerRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateLastLoginRequest;
import com.mygitgor.seller_service.application.dto.request.UpdateSellerRequest;
import com.mygitgor.seller_service.application.dto.request.VerifyDocumentsRequest;
import com.mygitgor.seller_service.application.dto.response.SellerRegistrationResponse;
import com.mygitgor.seller_service.application.dto.response.SellerResponse;
import com.mygitgor.seller_service.application.dto.response.UserAuthInfoResponse;
import com.mygitgor.seller_service.application.service.SellerAdminService;
import com.mygitgor.seller_service.application.service.SellerApplicationService;
import com.mygitgor.seller_service.application.service.SellerQueryService;
import com.mygitgor.seller_service.application.service.SellerRegistrationService;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import com.mygitgor.seller_service.shared.valueobject.Email;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/sellers")
@RequiredArgsConstructor
public class SellerInternalController {
    private final SellerQueryService queryService;
    private final SellerRegistrationService registrationService;
    private final SellerApplicationService applicationService;
    private final SellerAdminService adminService;
    private final SellerMapper sellerMapper;

    @GetMapping(value = "/exists/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Boolean> existsByEmail(@PathVariable String email) {
        log.debug("Internal API: Checking if seller exists by email: {}", email);
        return queryService.existsByEmail(new Email(email));
    }

    @GetMapping(value = "/{email}/auth-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<UserAuthInfoResponse> getSellerByEmail(@PathVariable String email) {
        log.info("Internal API: Fetching auth info for seller email: {}", email);
        return queryService.getSellerAuthInfoByEmail(new Email(email));
    }

    @GetMapping(value = "/{sellerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> getSellerById(@PathVariable String sellerId) {
        log.debug("Internal API: Fetching seller by ID: {}", sellerId);
        return queryService.getSellerById(new SellerId(sellerId))
                .map(sellerMapper::toResponse);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SellerRegistrationResponse> createSeller(@Valid @RequestBody RegisterSellerRequest request) {
        log.info("Internal API: Programmatic registration request for email: {}", request.email());
        return registrationService.registerSeller(request);
    }

    @PutMapping(value = "/{sellerId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> updateSeller(@PathVariable String sellerId, @Valid @RequestBody UpdateSellerRequest request) {
        log.info("Internal API: Request to update profile for seller ID: {}", sellerId);
        return applicationService.updateSeller(new SellerId(sellerId), request)
                .map(sellerMapper::toResponse);
    }

    @PatchMapping(value = "/{email}/verify-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> verifySellerEmail(@PathVariable String email) {
        log.info("Internal API: Manual status email confirmation for seller: {}", email);
        return applicationService.verifyEmail(new Email(email))
                .map(sellerMapper::toResponse);
    }

    @PostMapping(value = "/{sellerId}/verify-documents", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> verifySellerDocuments(@PathVariable String sellerId,
                                                      @Valid @RequestBody VerifyDocumentsRequest request
    ) {
        log.info("Internal API: Admin evaluation of documents for seller: {}, Approved={}", sellerId, request.approve());

        if (request.approve()) {
            return adminService.verifySellerBusiness(new SellerId(sellerId), request.verifiedBy(), request.notes())
                    .map(sellerMapper::toResponse);
        } else {
            return adminService.rejectSellerVerification(new SellerId(sellerId), request.notes(), request.verifiedBy())
                    .map(sellerMapper::toResponse);
        }
    }

    @PatchMapping(value = "/{sellerId}/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> updateAccountStatus(@PathVariable String sellerId,
                                                    @RequestBody Map<String, String> payload
    ) {
        String status = payload.get("status");
        String reason = payload.get("reason");
        log.info("Internal API: Changing status for seller {} to {}", sellerId, status);

        SellerId id = new SellerId(sellerId);
        return switch (status.toUpperCase()) {
            case "SUSPENDED" -> adminService.suspendSeller(id, reason, "SYSTEM_INTERNAL").map(sellerMapper::toResponse);
            case "BANNED", "BAN" -> adminService.banSeller(id, reason, "SYSTEM_INTERNAL").map(sellerMapper::toResponse);
            default -> Mono.error(new IllegalArgumentException("Unsupported account status mutation via PATCH: " + status));
        };
    }

    @PatchMapping(value = "/{sellerId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SellerResponse> activateSeller(@PathVariable String sellerId) {
        log.info("Internal API: Activating account for seller: {}", sellerId);
        return adminService.activateSeller(new SellerId(sellerId), "SYSTEM_INTERNAL")
                .map(sellerMapper::toResponse);
    }

    @PatchMapping(value = "/{sellerId}/last-active", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateLastActive(@PathVariable String sellerId,
                                       @RequestBody Map<String, LocalDateTime> payload
    ) {
        LocalDateTime lastActiveAt = payload.get("lastActiveAt");
        log.debug("Internal Heartbeat: Seller {} is active at {}", sellerId, lastActiveAt);
        return applicationService.updateLastActiveTime(new SellerId(sellerId), lastActiveAt);
    }

    @PatchMapping(value = "/{email}/last-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateLastLogin(@PathVariable String email, @Valid @RequestBody UpdateLastLoginRequest request) {
        log.info("Internal API: Logged successful login event for seller: {}", email);
        return applicationService.updateLastLoginByEmail(new Email(email), request.lastLoginAt());
    }
}
