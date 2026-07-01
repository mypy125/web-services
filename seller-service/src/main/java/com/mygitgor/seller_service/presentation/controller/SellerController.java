package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.service.SellerApplicationService;
import com.mygitgor.seller_service.application.service.SellerQueryService;
import com.mygitgor.seller_service.application.service.SellerRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {
    private final SellerRegistrationService registrationService;
    private final SellerApplicationService sellerApplicationService;
    private final SellerQueryService sellerQueryService;

}
