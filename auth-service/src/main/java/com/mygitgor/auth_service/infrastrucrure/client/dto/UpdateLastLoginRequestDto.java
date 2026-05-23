package com.mygitgor.auth_service.infrastrucrure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLastLoginRequestDto {
    private String email;
    private LocalDateTime lastLoginAt;
}
