package com.mygitgor.user_service.infrastructure.client.exception;

public class AddressServiceException extends ServiceClientException {

    public AddressServiceException(String operation, String message) {
        super("Address Service", operation, message);
    }

    public AddressServiceException(String operation, int statusCode, String message) {
        super("Address Service", operation, statusCode, message);
    }

    public AddressServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Address Service", operation, statusCode, message, cause);
    }

    public static AddressServiceException notFound(String identifier) {
        return new AddressServiceException("FIND_ADDRESS", 404,
                "Address not found: " + identifier);
    }

    public static AddressServiceException defaultAddressNotFound(String userId) {
        return new AddressServiceException("GET_DEFAULT", 404,
                "Default address not found for user: " + userId);
    }

    public static AddressServiceException invalidRequest(String identifier, String errorBody) {
        return new AddressServiceException("VALIDATE", 400,
                "Invalid request for address: " + identifier + " - " + errorBody);
    }

    public static AddressServiceException conflict(String identifier, String errorBody) {
        return new AddressServiceException("CONFLICT", 409,
                "Address conflict: " + identifier + " - " + errorBody);
    }

    public static AddressServiceException updateFailed(String userId, String addressId) {
        return new AddressServiceException("UPDATE_DEFAULT", 400,
                "Failed to update default address for user: " + userId + ", address: " + addressId);
    }

    public static AddressServiceException serviceError(String operation, String message) {
        return new AddressServiceException(operation, 500,
                "Address service error: " + message);
    }

    public static AddressServiceException timeout(String operation) {
        return new AddressServiceException(operation, 408,
                "Address service timeout for operation: " + operation);
    }
}
