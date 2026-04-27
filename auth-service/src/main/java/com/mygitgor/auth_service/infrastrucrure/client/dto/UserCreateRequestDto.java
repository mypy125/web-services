package com.mygitgor.auth_service.infrastrucrure.client.dto;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
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
public class UserCreateRequestDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String fullName;
    private UserRole role;
    private String phoneNumber;
    private String profileImage;
}
