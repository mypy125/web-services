package com.mygitgor.user_service.infrastructure.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUsersRequest {
    private String searchTerm;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String role;
    private String accountStatus;
    private Boolean emailVerified;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
