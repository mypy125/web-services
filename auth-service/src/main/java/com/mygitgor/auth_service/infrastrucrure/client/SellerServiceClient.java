package com.mygitgor.auth_service.infrastrucrure.client;

import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import com.mygitgor.auth_service.application.dto.common.AddressDto;
import com.mygitgor.auth_service.application.dto.common.BankDetailsDto;
import com.mygitgor.auth_service.application.dto.common.BusinessDetailsDto;
import com.mygitgor.auth_service.application.dto.request.seller.SellerUpdateRequestDto;
import com.mygitgor.auth_service.domain.seller.model.*;
import com.mygitgor.auth_service.domain.seller.model.valueobject.Address;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BankDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.BusinessDetails;
import com.mygitgor.auth_service.domain.seller.model.valueobject.SellerId;
import com.mygitgor.auth_service.domain.seller.port.SellerPort;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import com.mygitgor.auth_service.infrastrucrure.client.dto.*;
import com.mygitgor.auth_service.infrastrucrure.client.exception.SellerNotFoundException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceClientException;
import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceUnavailableException;
import com.mygitgor.auth_service.infrastrucrure.client.fallback.SellerServiceFallback;
import com.mygitgor.auth_service.infrastrucrure.client.interceptor.ServiceClientInterceptor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerServiceClient implements SellerPort {

    private final WebClient.Builder webClientBuilder;
    private final ServiceClientInterceptor clientInterceptor;
    private final SellerServiceFallback fallback;

    @Value("${seller.service.url:http://localhost:8083/api/sellers}")
    private String baseUrl;

    @Value("${seller.service.timeout:5000}")
    private int timeout;

    @Value("${seller.service.retry.attempts:3}")
    private int retryAttempts;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .filter(clientInterceptor.logRequest())
                .filter(clientInterceptor.logResponse())
                .filter(clientInterceptor.handleErrors())
                .build();
    }

    private Mono<Throwable> handleClientErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Client error during {} for {}: Status={}", operation, identifier, response.statusCode());

        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .map(errorBody -> {
                    if (response.statusCode().value() == 404) {
                        return new SellerNotFoundException("Seller not found: " + identifier);
                    }
                    if (response.statusCode().value() == 400) {
                        return new IllegalArgumentException("Bad request: " + errorBody);
                    }
                    if (response.statusCode().value() == 409) {
                        return new RuntimeException("Seller already exists: " + identifier);
                    }
                    return new ServiceClientException("Client error during " + operation + ": " + errorBody);
                });
    }

    private Mono<Throwable> handleServerErrorResponse(ClientResponse response, String operation, String identifier) {
        log.error("Server error during {} for {}: Status={}", operation, identifier, response.statusCode());
        return Mono.just(new ServiceUnavailableException("Seller service unavailable during " + operation));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "existsByEmailFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Boolean> existsByEmail(Email email) {
        log.debug("Checking if seller exists by email: {}", email);

        return webClient.get()
                .uri("/exists/{email}", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "exists by email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "exists by email", email.toString()))
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable ||
                                throwable instanceof java.net.ConnectException))
                .onErrorReturn(false)
                .doOnSuccess(result -> log.debug("Seller exists by email {}: {}", email, result));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "getSellerByEmailFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> getSellerByEmail(Email email) {
        log.info("Fetching seller by email: {}", email);

        return webClient.get()
                .uri("/{email}/auth-info", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found with email: " + email)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get seller by email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get seller by email", email.toString()))
                .bodyToMono(SellerAuthInfoDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(seller -> log.info("Successfully fetched seller: {}", email))
                .doOnError(error -> log.error("Failed to fetch seller by email {}: {}", email, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "getSellerByIdFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> getSellerById(SellerId sellerId) {
        log.debug("Fetching seller by ID: {}", sellerId);

        return webClient.get()
                .uri("/{sellerId}", sellerId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found with ID: " + sellerId)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get seller by id", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get seller by id", sellerId.toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(seller -> log.debug("Seller fetched successfully: {}", sellerId))
                .doOnError(error -> log.error("Failed to fetch seller by ID {}: {}", sellerId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "getSellerByUserIdFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> getSellerByUserId(UserId userId) {
        log.debug("Fetching seller by user ID: {}", userId);

        return webClient.get()
                .uri("/user/{userId}", userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found for user: " + userId)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "get seller by user id", userId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "get seller by user id", userId.toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(seller -> log.debug("Seller fetched by user ID: {}", userId));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "createSellerFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> createSeller(Seller seller) {
        log.info("Creating new seller: {}", seller.getEmail());

        SellerCreateRequestDto request = SellerCreateRequestDto.builder()
                .sellerName(seller.getSellerName())
                .email(seller.getEmail().toString())
                .mobile(seller.getMobile())
                .businessDetails(toBusinessDetailsDto(seller.getBusinessDetails()))
                .bankDetails(toBankDetailsDto(seller.getBankDetails()))
                .pickupAddress(toAddressDto(seller.getPickupAddress()))
                .gstNumber(seller.getGstNumber())
                .panNumber(seller.getPanNumber())
                .storeLogo(seller.getStoreLogo())
                .storeBanner(seller.getStoreBanner())
                .storeDescription(seller.getStoreDescription())
                .build();

        return webClient.post()
                .uri("/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode.CONFLICT::equals, response ->
                        handleClientErrorResponse(response, "create seller", seller.getEmail().toString()))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "create seller", seller.getEmail().toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "create seller", seller.getEmail().toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable))
                .doOnSuccess(created -> log.info("Seller created successfully: {}", seller.getEmail()))
                .doOnError(error -> log.error("Failed to create seller {}: {}", seller.getEmail(), error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "updateSellerFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> updateSeller(Seller seller) {
        log.info("Updating seller: {}", seller.getEmail());

        SellerUpdateRequestDto request = SellerUpdateRequestDto.builder()
                .sellerName(seller.getSellerName())
                .mobile(seller.getMobile())
                .businessDetails(toBusinessDetailsDto(seller.getBusinessDetails()))
                .bankDetails(toBankDetailsDto(seller.getBankDetails()))
                .pickupAddress(toAddressDto(seller.getPickupAddress()))
                .gstNumber(seller.getGstNumber())
                .panNumber(seller.getPanNumber())
                .storeLogo(seller.getStoreLogo())
                .storeBanner(seller.getStoreBanner())
                .storeDescription(seller.getStoreDescription())
                .build();

        return webClient.put()
                .uri("/{sellerId}", seller.getSellerId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found: " + seller.getEmail())))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update seller", seller.getEmail().toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update seller", seller.getEmail().toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(updated -> log.info("Seller updated successfully: {}", seller.getEmail()))
                .doOnError(error -> log.error("Failed to update seller {}: {}", seller.getEmail(), error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "sellerService", fallbackMethod = "verifySellerEmailFallback")
    @Retry(name = "sellerService")
    @TimeLimiter(name = "sellerService")
    public Mono<Seller> verifySellerEmail(Email email) {
        log.info("Verifying email for seller: {}", email);

        return webClient.patch()
                .uri("/{email}/verify-email", email.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found: " + email)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "verify email", email.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "verify email", email.toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(verified -> log.info("Email verified successfully for seller: {}", email))
                .doOnError(error -> log.error("Failed to verify email for seller {}: {}", email, error.getMessage()));
    }

    @Override
    public Mono<Seller> verifyBusinessDocuments(SellerId sellerId, String verifiedBy) {
        return null;
    }

    @Override
    @CircuitBreaker(name = "sellerService")
    @Retry(name = "sellerService")
    public Mono<Seller> updateAccountStatus(SellerId sellerId, String status, String reason) {
        log.info("Updating account status for seller {} to {}", sellerId, status);

        return webClient.patch()
                .uri("/{sellerId}/status", sellerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("status", status, "reason", reason))
                .retrieve()
                .onStatus(HttpStatusCode.NOT_FOUND::equals, response ->
                        Mono.error(new SellerNotFoundException("Seller not found: " + sellerId)))
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update account status", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update account status", sellerId.toString()))
                .bodyToMono(SellerDto.class)
                .map(this::toDomainSeller)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(seller -> log.info("Account status updated for seller: {}", sellerId))
                .doOnError(error -> log.error("Failed to update account status for seller {}: {}", sellerId, error.getMessage()));
    }

    @Override
    public Mono<Seller> updatePayoutSettings(SellerId sellerId, BankDetails bankDetails) {
        return null;
    }

    @Override
    public Mono<Seller> updateBusinessDetails(SellerId sellerId, BusinessDetails businessDetails) {
        return null;
    }

    @Override
    public Mono<Seller> updatePickupAddress(SellerId sellerId, Address pickupAddress) {
        return null;
    }

    @Override
    public Mono<SellerBalance> getSellerBalance(SellerId sellerId) {
        return null;
    }

    @Override
    public Flux<PayoutTransaction> getPayoutHistory(SellerId sellerId, int limit, int offset) {
        return null;
    }

    @Override
    public Mono<SellerStatistics> getSellerStatistics(SellerId sellerId, LocalDateTime startDate, LocalDateTime endDate) {
        return null;
    }

    @Override
    public Mono<Long> getProductCount(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Double> getTotalSales(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<SellerRating> getSellerRating(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> canAddProducts(SellerId sellerId) {
        return null;
    }

    @Override
    public Mono<Boolean> isSellerVerified(Email email) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getAllSellers(String status, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> searchSellers(String searchTerm, int page, int size) {
        return null;
    }

    @Override
    public Mono<Page<Seller>> getPendingVerifications(int page, int size) {
        return null;
    }

    @Override
    @CircuitBreaker(name = "sellerService")
    @Retry(name = "sellerService")
    public Mono<Void> updateLastActive(SellerId sellerId, LocalDateTime lastActiveAt) {
        log.debug("Updating last active for seller: {} at {}", sellerId, lastActiveAt);

        return webClient.patch()
                .uri("/{sellerId}/last-active", sellerId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("lastActiveAt", lastActiveAt))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        handleClientErrorResponse(response, "update last active", sellerId.toString()))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        handleServerErrorResponse(response, "update last active", sellerId.toString()))
                .toBodilessEntity()
                .timeout(Duration.ofMillis(timeout))
                .then()
                .doOnSuccess(v -> log.debug("Last active updated for seller: {}", sellerId))
                .doOnError(error -> log.warn("Failed to update last active for seller {}: {}", sellerId, error.getMessage()));
    }

    @Override
    public Mono<Seller> updateCommissionRate(SellerId sellerId, double commissionRate) {
        return null;
    }

    private Mono<Boolean> existsByEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: existsByEmail for {} due to: {}", email, t.getMessage());
        return fallback.existsByEmail(email);
    }

    private Mono<Seller> getSellerByEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: getSellerByEmail for {} due to: {}", email, t.getMessage());
        return fallback.getSellerByEmail(email);
    }

    private Mono<Seller> getSellerByIdFallback(SellerId sellerId, Throwable t) {
        log.warn("Fallback: getSellerById for {} due to: {}", sellerId, t.getMessage());
        return fallback.getSellerById(sellerId);
    }

    private Mono<Seller> getSellerByUserIdFallback(UserId userId, Throwable t) {
        log.warn("Fallback: getSellerByUserId for {} due to: {}", userId, t.getMessage());
        return fallback.getSellerByUserId(userId);
    }

    private Mono<Seller> createSellerFallback(Seller seller, Throwable t) {
        log.warn("Fallback: createSeller for {} due to: {}", seller.getEmail(), t.getMessage());
        return fallback.createSeller(seller);
    }

    private Mono<Seller> updateSellerFallback(Seller seller, Throwable t) {
        log.warn("Fallback: updateSeller for {} due to: {}", seller.getEmail(), t.getMessage());
        return fallback.updateSeller(seller);
    }

    private Mono<Seller> verifySellerEmailFallback(Email email, Throwable t) {
        log.warn("Fallback: verifySellerEmail for {} due to: {}", email, t.getMessage());
        return fallback.verifySellerEmail(email);
    }

    private Mono<Seller> updateAccountStatusFallback(SellerId sellerId, String status, String reason, Throwable t) {
        log.warn("Fallback: updateAccountStatus for seller {} due to: {}", sellerId, t.getMessage());
        return fallback.updateAccountStatus(sellerId, status, reason);
    }

    private Seller toDomainSeller(SellerDto dto) {
        if (dto == null) return null;

        return Seller.builder()
                .sellerId(new SellerId(dto.getId()))
                .email(new Email(dto.getEmail()))
                .sellerName(dto.getSellerName())
                .mobile(dto.getMobile())
                .role(dto.getRole())
                .emailVerified(dto.isEmailVerified())
                .verificationStatus(dto.getVerificationStatus())
                .accountStatus(dto.getAccountStatus())
                .businessDetails(toDomainBusinessDetails(dto.getBusinessDetails()))
                .bankDetails(toDomainBankDetails(dto.getBankDetails()))
                .pickupAddress(toDomainAddress(dto.getPickupAddress()))
                .gstNumber(dto.getGstNumber())
                .panNumber(dto.getPanNumber())
                .commissionRate(dto.getCommissionRate())
                .storeLogo(dto.getStoreLogo())
                .storeBanner(dto.getStoreBanner())
                .storeDescription(dto.getStoreDescription())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .emailVerifiedAt(dto.getEmailVerifiedAt())
                .businessVerifiedAt(dto.getBusinessVerifiedAt())
                .lastActiveAt(dto.getLastActiveAt())
                .rejectionReason(dto.getRejectionReason())
                .build();
    }

    private Seller toDomainSeller(SellerAuthInfoDto dto) {
        if (dto == null) return null;

        return Seller.builder()
                .sellerId(new SellerId(dto.getId()))
                .email(new Email(dto.getEmail()))
                .sellerName(dto.getSellerName())
                .role(dto.getRole())
                .emailVerified(dto.isEmailVerified())
                .build();
    }

    private BusinessDetails toDomainBusinessDetails(BusinessDetailsDto dto) {
        if (dto == null) return null;

        return BusinessDetails.builder()
                .businessName(dto.getBusinessName())
                .businessEmail(new Email(dto.getBusinessEmail()))
                .businessMobile(dto.getBusinessMobile())
                .businessAddress(dto.getBusinessAddress())
                .registrationNumber(dto.getRegistrationNumber())
                .taxId(dto.getTaxId())
                .website(dto.getWebsite())
                .description(dto.getDescription())
                .businessType(dto.getBusinessType())
                .build();
    }

    private BankDetails toDomainBankDetails(BankDetailsDto dto) {
        if (dto == null) return null;

        return BankDetails.builder()
                .accountNumber(dto.getAccountNumber())
                .accountHolderName(dto.getAccountHolderName())
                .bankName(dto.getBankName())
                .bankCode(dto.getBankCode())
                .accountType(dto.getAccountType())
                .upiId(dto.getUpiId())
                .build();
    }

    private Address toDomainAddress(AddressDto dto) {
        if (dto == null) return null;

        return Address.builder()
                .name(dto.getName())
                .locality(dto.getLocality())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .pinCode(dto.getPinCode())
                .mobile(dto.getMobile())
                .build();
    }

    private BusinessDetailsDto toBusinessDetailsDto(BusinessDetails domain) {
        if (domain == null) return null;

        return BusinessDetailsDto.builder()
                .businessName(domain.getBusinessName())
                .businessEmail(domain.getBusinessEmail() != null ? domain.getBusinessEmail().toString() : null)
                .businessMobile(domain.getBusinessMobile())
                .businessAddress(domain.getBusinessAddress())
                .registrationNumber(domain.getRegistrationNumber())
                .taxId(domain.getTaxId())
                .website(domain.getWebsite())
                .description(domain.getDescription())
                .businessType(domain.getBusinessType())
                .build();
    }

    private BankDetailsDto toBankDetailsDto(BankDetails domain) {
        if (domain == null) return null;

        return BankDetailsDto.builder()
                .accountNumber(domain.getAccountNumber())
                .accountHolderName(domain.getAccountHolderName())
                .bankName(domain.getBankName())
                .bankCode(domain.getBankCode())
                .accountType(domain.getAccountType())
                .upiId(domain.getUpiId())
                .build();
    }

    private AddressDto toAddressDto(Address domain) {
        if (domain == null) return null;

        return AddressDto.builder()
                .name(domain.getName())
                .locality(domain.getLocality())
                .address(domain.getAddress())
                .city(domain.getCity())
                .state(domain.getState())
                .pinCode(domain.getPinCode())
                .mobile(domain.getMobile())
                .build();
    }
}