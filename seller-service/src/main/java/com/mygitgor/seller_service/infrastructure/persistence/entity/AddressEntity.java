package com.mygitgor.seller_service.infrastructure.persistence.entity;

import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import java.util.UUID;

@Table("seller_addresses")
public record AddressEntity(
        @Id UUID id,
        @Column("seller_id") UUID sellerId,
        String name,
        @Column("address_line1") String addressLine1,
        @Column("address_line2") String addressLine2,
        String city,
        String state,
        @Column("postal_code") String postalCode,
        String country,
        @Column("phone_number") String phoneNumber,
        String landmark,
        Double latitude,
        Double longitude,
        @Column("address_type") String addressType
) {}
