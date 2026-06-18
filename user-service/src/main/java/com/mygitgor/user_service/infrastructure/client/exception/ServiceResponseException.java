package com.mygitgor.user_service.infrastructure.client.exception;

public class ServiceResponseException extends ServiceClientException {

    private final String responseBody;

    public ServiceResponseException(String serviceName, String operation, int statusCode, String responseBody) {
        super(serviceName, operation, statusCode, "Invalid response from service");
        this.responseBody = responseBody;
    }

    public ServiceResponseException(String serviceName, String operation, int statusCode, String message, String responseBody) {
        super(serviceName, operation, statusCode, message);
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
