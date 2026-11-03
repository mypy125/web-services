package com.mygitgor.seller_service.infrastructure.persistence.projection;

import java.time.LocalDateTime;

public interface PeriodSummaryProjection {
    String getPeriod();
    LocalDateTime getPeriodStart();
    LocalDateTime getPeriodEnd();
    Integer getTotalOrders();
    Double getTotalSales();
    Double getTotalEarnings();
    Double getAverageOrderValue();
    Integer getTotalTransactions();
    Double getTotalCommission();
}