package com.mygitgor.user_service.infrastructure.dto.request;

import com.mygitgor.user_service.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String fullName;

    private UserRole role;
    private String phoneNumber;
    private String profileImage;
}
