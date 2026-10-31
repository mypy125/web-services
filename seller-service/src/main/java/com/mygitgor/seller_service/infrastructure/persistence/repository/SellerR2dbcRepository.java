package com.mygitgor.seller_service.infrastructure.persistence.repository;

import com.mygitgor.seller_service.infrastructure.persistence.entity.SellerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface SellerR2dbcRepository extends ReactiveCrudRepository<SellerEntity, UUID> {
    Mono<SellerEntity> findByEmail(String email);
    Mono<SellerEntity> findByStoreName(String storeName);
    Flux<SellerEntity> findAllBy(Pageable pageable);
    Flux<SellerEntity> findByAccountStatus(String status, Pageable pageable);
    Flux<SellerEntity> findByVerificationStatus(String verificationStatus, Pageable pageable);
    Flux<SellerEntity> findByStoreCategory(String category, Pageable pageable);
    Flux<SellerEntity> findByAverageRatingGreaterThan(Double minRating, Pageable pageable);
    Flux<SellerEntity> findByIdIn(List<UUID> ids);
    Flux<SellerEntity> findByEmailIn(List<String> emails);
    Mono<Long> countByAccountStatus(String status);
    Mono<Long> countByVerificationStatus(String verificationStatus);
    Mono<Long> countByStoreCategory(String category);
    Mono<Long> countByAverageRatingGreaterThan(Double minRating);
    Mono<Boolean> existsByEmail(String email);
    Mono<Void> deleteByEmail(String email);

    @Query("SELECT COALESCE(AVG(average_rating), 0.0) FROM sellers")
    Mono<Double> getAverageRating();

    @Query("SELECT COALESCE(SUM(total_sales), 0.0) FROM sellers")
    Mono<Double> getTotalSales();

    @Query("SELECT COALESCE(SUM(total_orders), 0) FROM sellers")
    Mono<Integer> getTotalOrdersCount();

    @Query("SELECT * FROM sellers WHERE average_rating > 0 ORDER BY average_rating DESC LIMIT :limit")
    Flux<SellerEntity> findTopRated(Pageable pageable);

    @Query("SELECT * FROM sellers WHERE " +
            "LOWER(seller_name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(store_name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(email) LIKE LOWER(CONCAT('%', :term, '%'))")
    Flux<SellerEntity> search(String term, Pageable pageable);

    @Query("SELECT COUNT(*) FROM sellers WHERE " +
            "LOWER(seller_name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(store_name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(email) LIKE LOWER(CONCAT('%', :term, '%'))")
    Mono<Long> countBySearchTerm(String term);

    @Query("SELECT COALESCE(AVG(average_order_value), 0) FROM sellers")
    Mono<Double> getAverageOrderValue();

    @Query("SELECT COALESCE(AVG(commission_rate), 0) FROM sellers")
    Mono<Double> getAverageCommissionRate();

    @Query("SELECT COALESCE(AVG(response_rate), 0) FROM sellers")
    Mono<Double> getAverageResponseRate();

    @Query("SELECT COALESCE(AVG(response_time_hours), 0) FROM sellers")
    Mono<Double> getAverageResponseTimeHours();

    @Query("SELECT COALESCE(SUM(total_earnings), 0) FROM sellers")
    Mono<Double> getTotalEarnings();

    @Query("SELECT COALESCE(SUM(total_commission_paid), 0) FROM sellers")
    Mono<Double> getTotalCommissionPaid();

    @Query("SELECT COALESCE(SUM(total_products), 0) FROM sellers")
    Mono<Integer> getTotalProductsCount();
}
