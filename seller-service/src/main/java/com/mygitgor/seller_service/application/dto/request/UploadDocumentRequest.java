package com.mygitgor.seller_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record UploadDocumentRequest(
        @NotNull(message = "Document type is required")
        String documentType,

        @NotBlank(message = "Document URL cannot be empty")
        @URL(message = "Document URL must be a valid link")
        String documentUrl,

        @NotBlank(message = "Document number is required")
        String documentNumber
) {}
