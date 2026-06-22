package com.mygitgor.seller_service.domain.model.shared.valueobject;

import lombok.Builder;
import java.time.DayOfWeek;
import java.util.Map;

@Builder
public record BusinessHours(
        Map<DayOfWeek, BusinessDaySchedule> schedule,
        boolean isAlwaysOpen,
        String timezone
) {

    @Builder
    public record BusinessDaySchedule(
            String openTime,
            String closeTime,
            boolean isClosed
    ) {}
}
