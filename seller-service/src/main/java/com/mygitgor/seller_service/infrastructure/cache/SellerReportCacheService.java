package com.mygitgor.seller_service.infrastructure.cache;

import com.mygitgor.seller_service.domain.model.ReportPeriod;
import com.mygitgor.seller_service.domain.model.SellerReport;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerReportId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerReportCacheService {
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration REPORT_CACHE_TTL = Duration.ofDays(1);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public Mono<Void> cacheReport(SellerReport report) {
        if (report == null || report.getReportId() == null) {
            return Mono.empty();
        }

        try {
            String jsonReport = objectMapper.writeValueAsString(report);
            String idKey = buildIdKey(report.getReportId());
            String periodKey = buildPeriodKey(report.getSellerId(), report.getPeriod(), report.getPeriodStart());

            log.debug("Caching report {} for seller {} via ReactiveStringRedisTemplate", report.getReportId(), report.getSellerId());

            return redisTemplate.opsForValue().set(idKey, jsonReport, REPORT_CACHE_TTL)
                    .then(redisTemplate.opsForValue().set(periodKey, jsonReport, REPORT_CACHE_TTL))
                    .then();

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SellerReport to JSON for report: {}", report.getReportId(), e);
            return Mono.empty();
        }
    }

    public Mono<SellerReport> getCachedReportById(SellerReportId reportId) {
        if (reportId == null) {
            return Mono.empty();
        }
        return redisTemplate.opsForValue().get(buildIdKey(reportId))
                .flatMap(this::deserializeReport);
    }

    public Mono<SellerReport> getCachedReportByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        if (sellerId == null || period == null || startDate == null) {
            return Mono.empty();
        }
        String periodKey = buildPeriodKey(sellerId, period, startDate);
        return redisTemplate.opsForValue().get(periodKey)
                .flatMap(this::deserializeReport);
    }

    public Mono<Void> evictReportCache(SellerReport report) {
        if (report == null) {
            return Mono.empty();
        }
        String idKey = buildIdKey(report.getReportId());
        String periodKey = buildPeriodKey(report.getSellerId(), report.getPeriod(), report.getPeriodStart());

        return redisTemplate.delete(idKey, periodKey).then();
    }

    public Mono<Void> evictReportByPeriod(SellerId sellerId, ReportPeriod period, LocalDateTime startDate, LocalDateTime endDate) {
        if (sellerId == null || period == null || startDate == null) {
            return Mono.empty();
        }
        String periodKey = buildPeriodKey(sellerId, period, startDate);
        return redisTemplate.delete(periodKey).then();
    }

    public Mono<Void> evictAllReportsForSeller(SellerId sellerId) {
        if (sellerId == null) {
            return Mono.empty();
        }

        String prefix = "seller:report:period:" + sellerId.toString() + ":";

        return redisTemplate.scan()
                .filter(key -> key.startsWith(prefix))
                .collectList()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        return Mono.empty();
                    }
                    return redisTemplate.delete(keys.toArray(new String[0]));
                })
                .then();
    }

    private String buildIdKey(SellerReportId reportId) {
        return "seller:report:id:" + reportId.toString();
    }

    private String buildPeriodKey(SellerId sellerId, ReportPeriod period, LocalDateTime startDate) {
        String formattedDate = startDate.format(DATE_FORMATTER);
        return String.format("seller:report:period:%s:%s:%s", sellerId.toString(), period.name(), formattedDate);
    }

    private Mono<SellerReport> deserializeReport(String json) {
        try {
            SellerReport report = objectMapper.readValue(json, SellerReport.class);
            return Mono.just(report);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize SellerReport from JSON", e);
            return Mono.empty();
        }
    }
}