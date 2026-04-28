package com.mygitgor.auth_service.infrastrucrure.sequrity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProps {
    private String secretKey;
    private String header = "Authorization";
    private Long expirationTime = 86400000L;
}
