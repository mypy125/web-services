package com.mygitgor.auth_service.domain.model.aggregate;

import com.mygitgor.auth_service.domain.model.enums.OtpPurpose;
import com.mygitgor.auth_service.domain.model.Token;
import com.mygitgor.auth_service.domain.model.enums.UserRole;
import com.mygitgor.auth_service.domain.model.VerificationCode;
import com.mygitgor.auth_service.domain.model.event.*;
import com.mygitgor.auth_service.domain.shared.exception.DomainException;
import com.mygitgor.auth_service.domain.shared.valueobject.Email;
import com.mygitgor.auth_service.domain.shared.valueobject.UserId;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthAggregate extends AbstractAggregateRoot<AuthAggregate> {
    private Email email;
    private UserId userId;
    private UserRole role;
    private Token currentToken;
    private List<VerificationCode> verificationCodes = new ArrayList<>();

    private AuthAggregate() {}

    public static AuthAggregate register(Email email, UserId userId, UserRole role) {
        AuthAggregate aggregate = new AuthAggregate();
        aggregate.email = email;
        aggregate.userId = userId;
        aggregate.role = role;

        aggregate.registerEvent(UserRegisteredEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .role(role)
                .occurredAt(LocalDateTime.now())
                .build());

        return aggregate;
    }

    public Token login(Token newToken) {
        this.currentToken = newToken;

        registerEvent(UserLoggedInEvent.builder()
                .email(email.toString())
                .userId(userId.toString())
                .token(newToken.getValue().toString())
                .occurredAt(LocalDateTime.now())
                .build());

        return newToken;
    }

    public void logout(String tokenValue) {
        if (currentToken != null && currentToken.getValue().toString().equals(tokenValue)) {
            currentToken.blacklist();

            registerEvent(TokenBlacklistedEvent.builder()
                    .token(tokenValue)
                    .email(email.toString())
                    .occurredAt(LocalDateTime.now())
                    .build());
        }
    }

    public void addVerificationCode(VerificationCode code) {
        verificationCodes.add(code);

        registerEvent(OtpGeneratedEvent.builder()
                .email(code.getEmail().toString())
                .otp(code.getOtp().getValue())
                .purpose(code.getPurpose())
                .occurredAt(LocalDateTime.now())
                .build());
    }

    public VerificationCode verifyOtp(String otpValue, OtpPurpose purpose) {
        VerificationCode validCode = verificationCodes.stream()
                .filter(code -> code.getOtp().getValue().equals(otpValue))
                .filter(code -> code.getPurpose() == purpose)
                .filter(VerificationCode::isValid)
                .findFirst()
                .orElseThrow(() -> new DomainException("Invalid or expired OTP"));

        validCode.markAsUsed();

        registerEvent(OtpVerifiedEvent.builder()
                .email(email.toString())
                .purpose(purpose)
                .occurredAt(LocalDateTime.now())
                .build());

        return validCode;
    }
}
