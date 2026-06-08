package com.mygitgor.auth_service.infrastrucrure.kafka.event.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifiedEvent {
    private String email;
    private String purpose;
    private boolean success;
    private LocalDateTime occurredAt;
}
