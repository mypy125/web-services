package com.mygitgor.user_service.domain.specification;

import com.mygitgor.user_service.domain.model.User;
import com.mygitgor.user_service.domain.model.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class UserRoleSpec {

    public Mono<Boolean> isSatisfiedByRole(User user, UserRole expectedRole) {
        if (user == null || expectedRole == null) {
            return Mono.just(false);
        }
        boolean hasRole = user.getRole() == expectedRole;
        log.debug("User {} has role {}: {}", user.getEmail(), expectedRole, hasRole);
        return Mono.just(hasRole);
    }

    public Mono<Boolean> isAdmin(User user) {
        return isSatisfiedByRole(user, UserRole.ROLE_ADMIN);
    }

    public Mono<Boolean> isSeller(User user) {
        return isSatisfiedByRole(user, UserRole.ROLE_SELLER);
    }


    public Mono<Boolean> isCustomer(User user) {
        return isSatisfiedByRole(user, UserRole.ROLE_CUSTOMER);
    }

    public Mono<Boolean> hasAnyRole(User user, UserRole... roles) {
        if (user == null || roles == null || roles.length == 0) {
            return Mono.just(false);
        }

        for (UserRole role : roles) {
            if (user.getRole() == role) {
                log.debug("User {} has role {}", user.getEmail(), role);
                return Mono.just(true);
            }
        }
        log.debug("User {} has none of the specified roles", user.getEmail());
        return Mono.just(false);
    }

    public Mono<Boolean> canPerformSellerOperations(User user) {
        return isSatisfiedByRole(user, UserRole.ROLE_SELLER)
                .or(isSatisfiedByRole(user, UserRole.ROLE_ADMIN));
    }

    public Mono<Boolean> canPerformAdminOperations(User user) {
        return isSatisfiedByRole(user, UserRole.ROLE_ADMIN);
    }
}
