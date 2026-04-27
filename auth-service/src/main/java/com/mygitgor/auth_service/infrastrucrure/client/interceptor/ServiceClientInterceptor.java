package com.mygitgor.auth_service.infrastrucrure.client.interceptor;

import com.mygitgor.auth_service.infrastrucrure.client.exception.ServiceClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class ServiceClientInterceptor {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";

    public ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String correlationId = getOrCreateCorrelationId(request);

            log.info("Request: {} {} [correlationId={}]",
                    request.method(), request.url(), correlationId);

            if (log.isDebugEnabled()) {
                request.headers().forEach((name, values) ->
                        log.debug("Header: {}={} [correlationId={}]", name, values, correlationId));
            }

            return Mono.just(ClientRequest.from(request)
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .header(SERVICE_NAME_HEADER, "auth-service")
                    .build());
        });
    }

    public ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("Response: {} [correlationId={}]",
                    response.statusCode(), getCorrelationId(response));

            return Mono.just(response);
        });
    }

    public ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is4xxClientError() ||
                    response.statusCode().is5xxServerError()) {

                return response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            log.error("Error response: {} - {}", response.statusCode(), errorBody);
                            return Mono.error(new ServiceClientException(
                                    String.format("Service returned %s: %s", response.statusCode(), errorBody)
                            ));
                        });
            }
            return Mono.just(response);
        });
    }

    private String getOrCreateCorrelationId(ClientRequest request) {
        String correlationId = request.headers().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private String getCorrelationId(ClientResponse response) {
        return response.headers().header(CORRELATION_ID_HEADER)
                .stream()
                .findFirst()
                .orElse("unknown");
    }
}
