package com.mygitgor.seller_service.application.dto.request;

public record SuspendOrBanRequest(
        String reason,
        String adminName
) {}
