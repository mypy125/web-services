package com.mygitgor.seller_service.domain.specification;

import com.mygitgor.seller_service.domain.model.Seller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerRatingSpec {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;
    private static final double HIGH_RATING_THRESHOLD = 4.5;
    private static final double GOOD_RESPONSE_RATE_THRESHOLD = 90.0;

    public Mono<Boolean> isValidRating(Integer rating) {
        if (rating == null) {
            return Mono.just(false);
        }
        boolean isValid = rating >= MIN_RATING && rating <= MAX_RATING;
        log.debug("Rating {} is valid: {}", rating, isValid);
        return Mono.just(isValid);
    }

    public Mono<Boolean> isHighRated(Seller seller) {
        if (seller == null || seller.getAverageRating() == null) {
            return Mono.just(false);
        }
        boolean isHighRated = seller.getAverageRating() >= HIGH_RATING_THRESHOLD;
        log.debug("Seller {} is high rated: {}", seller.getEmail(), isHighRated);
        return Mono.just(isHighRated);
    }

    public Mono<Boolean> hasReviews(Seller seller) {
        if (seller == null) {
            return Mono.just(false);
        }
        boolean hasReviews = seller.hasReviews();
        log.debug("Seller {} has reviews: {}", seller.getEmail(), hasReviews);
        return Mono.just(hasReviews);
    }

    public Mono<Boolean> hasGoodResponseRate(Seller seller) {
        if (seller == null || seller.getResponseRate() == null) {
            return Mono.just(false);
        }
        boolean hasGoodResponse = seller.getResponseRate() >= GOOD_RESPONSE_RATE_THRESHOLD;
        log.debug("Seller {} has good response rate: {}", seller.getEmail(), hasGoodResponse);
        return Mono.just(hasGoodResponse);
    }
}
