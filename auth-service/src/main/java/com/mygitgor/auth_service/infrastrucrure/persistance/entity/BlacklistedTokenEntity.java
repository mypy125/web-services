package com.mygitgor.auth_service.infrastrucrure.persistance.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("blacklisted_tokens")
public class BlacklistedTokenEntity {
    @Id
    private UUID id;
    private String token;
    private UUID userId;
    private LocalDateTime blacklistedAt;
    private LocalDateTime expiresAt;

    public BlacklistedTokenEntity(String token, UUID userId, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.blacklistedAt = LocalDateTime.now();
    }
}
