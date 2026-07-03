package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.request.*;
import com.mygitgor.seller_service.application.dto.response.SellerResponse;
import com.mygitgor.seller_service.application.service.SellerAdminService;
import com.mygitgor.seller_service.domain.port.incoming.SellerQueryUseCase;
import com.mygitgor.seller_service.infrastructure.mapper.SellerMapper;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController {
    private final SellerQueryUseCase sellerQueryUseCase;
    private final SellerAdminService sellerAdminService;
    private final SellerMapper sellerMapper;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<SellerResponse>> getSellerById(@PathVariable String id) {
        log.info("Admin request: fetch seller by id {}", id);
        return sellerQueryUseCase.getSellerById(new SellerId(id))
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Page<SellerResponse>>> getAllSellers(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size
    ) {
        return sellerQueryUseCase.getAllSellers(page, size)
                .map(domainPage -> domainPage.map(sellerMapper::toResponse))
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/verify-business")
    public Mono<ResponseEntity<SellerResponse>> verifySellerBusiness(@PathVariable String id,
                                                                     @RequestBody VerifyBusinessRequest request,
                                                                     Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.verifiedBy();
        log.info("Admin '{}' requested business verification for seller {}", adminName, id);

        return sellerAdminService.verifySellerBusiness(new SellerId(id), adminName, request.notes())
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/verify-tax")
    public Mono<ResponseEntity<SellerResponse>> verifySellerTaxInfo(@PathVariable String id,
                                                                    @RequestBody VerifyTaxRequest request,
                                                                    Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.verifiedBy();

        return sellerAdminService.verifySellerTaxInfo(new SellerId(id), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/reject-verification")
    public Mono<ResponseEntity<SellerResponse>> rejectSellerVerification(@PathVariable String id,
                                                                         @RequestBody RejectVerificationRequest request,
                                                                         Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.rejectedBy();

        return sellerAdminService.rejectSellerVerification(new SellerId(id), request.reason(), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/activate")
    public Mono<ResponseEntity<SellerResponse>> activateSeller(@PathVariable String id,
                                                               @RequestBody ActionByAdminRequest request,
                                                               Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.adminName();

        return sellerAdminService.activateSeller(new SellerId(id), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/suspend")
    public Mono<ResponseEntity<SellerResponse>> suspendSeller(@PathVariable String id,
                                                              @RequestBody SuspendOrBanRequest request,
                                                              Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.adminName();

        return sellerAdminService.suspendSeller(new SellerId(id), request.reason(), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/ban")
    public Mono<ResponseEntity<SellerResponse>> banSeller(@PathVariable String id,
                                                          @RequestBody SuspendOrBanRequest request,
                                                          Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.adminName();

        return sellerAdminService.banSeller(new SellerId(id), request.reason(), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/commission-rate")
    public Mono<ResponseEntity<SellerResponse>> updateSellerCommissionRate(@PathVariable String id,
                                                                           @RequestBody UpdateRateRequest request,
                                                                           Principal principal
    ) {

        String adminName = principal != null ? principal.getName() : request.updatedBy();

        return sellerAdminService.updateSellerCommissionRate(new SellerId(id), request.newRate(), adminName)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/cashback-rate")
    public Mono<ResponseEntity<SellerResponse>> updateSellerCashbackRate(@PathVariable String id,
                                                                         @RequestParam Double cashbackRate
    ) {

        log.info("Request to update cashback rate to {}% for seller {}", cashbackRate, id);
        return sellerAdminService.updateSellerCashbackRate(new SellerId(id), cashbackRate)
                .map(sellerMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
