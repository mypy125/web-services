package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {
    private String to;
    private String subject;
    private String templateName;
    private Object templateData;
    private String otp;
    private String purpose;
}
