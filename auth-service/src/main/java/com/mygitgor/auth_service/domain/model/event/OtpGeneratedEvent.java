package com.mygitgor.auth_service.domain.model.event;

import com.mygitgor.auth_service.domain.model.enums.OtpPurpose;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class OtpGeneratedEvent extends ApplicationEvent {
    private final String email;
    private final String otp;
    private final OtpPurpose purpose;
    private final LocalDateTime expiresAt;
    private final LocalDateTime occurredAt;

    @Builder
    public OtpGeneratedEvent(Object source, String email, String otp, OtpPurpose purpose,
                             LocalDateTime expiresAt, LocalDateTime occurredAt) {
        super(source);
        this.email = email;
        this.otp = otp;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
        this.occurredAt = occurredAt;
    }
}
