package com.mygitgor.seller_service.domain.model.type;

import lombok.Getter;

@Getter
public enum BusinessType {
    RETAIL("Retail", "Retail trade business"),
    WHOLESALE("Wholesale", "Wholesale trade business"),
    ECOMMERCE("E-commerce", "Electronic commerce business"),
    DROPSHIPPING("Dropshipping", "Dropshipping business model"),

    MANUFACTURING("Manufacturing", "Manufacturing and industrial production"),
    HANDMADE("Handmade", "Handmade items and goods"),
    CRAFT("Craft", "Craft and artisanal production"),

    CONSULTING("Consulting", "Consulting services"),
    FREELANCE("Freelance", "Freelance and independent contracting"),
    AGENCY("Agency", "Agency and corporate services"),
    EDUCATION("Education", "Educational services"),
    TRAINING("Training", "Training and coaching services"),

    RESTAURANT("Restaurant", "Restaurant business"),
    CAFE("Cafe", "Cafe and coffee shop business"),
    HOTEL("Hotel", "Hotel and lodging hospitality"),
    CATERING("Catering", "Catering services"),

    SOFTWARE("Software", "Software development and products"),
    IT_SERVICES("IT Services", "Information technology services"),
    TELECOM("Telecom", "Telecommunications services"),

    HEALTHCARE("Healthcare", "Healthcare and medical services"),
    FITNESS("Fitness", "Fitness and gym services"),
    WELLNESS("Wellness", "Wellness and spa services"),

    NON_PROFIT("Non-Profit", "Non-profit organization"),
    GOVERNMENT("Government", "Government institutional entity"),
    OTHER("Other", "Other business type");

    private final String displayName;
    private final String description;

    BusinessType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isRetail() {
        return this == RETAIL || this == WHOLESALE || this == ECOMMERCE || this == DROPSHIPPING;
    }

    public boolean isManufacturing() {
        return this == MANUFACTURING || this == HANDMADE || this == CRAFT;
    }

    public boolean isService() {
        return this == CONSULTING || this == FREELANCE || this == AGENCY ||
                this == EDUCATION || this == TRAINING;
    }

    public boolean isHospitality() {
        return this == RESTAURANT || this == CAFE || this == HOTEL || this == CATERING;
    }

    public boolean isTechnology() {
        return this == SOFTWARE || this == IT_SERVICES || this == TELECOM;
    }

    public boolean isHealth() {
        return this == HEALTHCARE || this == FITNESS || this == WELLNESS;
    }

    public static BusinessType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return OTHER;
        }
        for (BusinessType type : values()) {
            if (type.displayName.equalsIgnoreCase(displayName.trim())) {
                return type;
            }
        }
        return OTHER;
    }

    public static BusinessType fromDescription(String description) {
        if (description == null || description.isBlank()) {
            return OTHER;
        }
        for (BusinessType type : values()) {
            if (type.description.equalsIgnoreCase(description.trim())) {
                return type;
            }
        }
        return OTHER;
    }
}
