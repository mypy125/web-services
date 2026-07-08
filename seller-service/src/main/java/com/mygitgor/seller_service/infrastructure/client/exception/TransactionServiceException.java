package com.mygitgor.seller_service.infrastructure.client.exception;

public class TransactionServiceException extends ServiceClientException {

    public TransactionServiceException(String operation, String message) {
        super("Transaction Service", operation, message);
    }

    public TransactionServiceException(String operation, int statusCode, String message) {
        super("Transaction Service", operation, statusCode, message);
    }

    public TransactionServiceException(String operation, int statusCode, String message, Throwable cause) {
        super("Transaction Service", operation, statusCode, message, cause);
    }

    public static TransactionServiceException transactionNotFound(String identifier) {
        return new TransactionServiceException("GET_TRANSACTION", 404,
                "Transaction not found: " + identifier);
    }

    public static TransactionServiceException invalidTransactionRequest(String operation, String errorBody) {
        return new TransactionServiceException(operation, 400,
                "Invalid transaction request: " + errorBody);
    }

    public static TransactionServiceException accessDenied(String identifier) {
        return new TransactionServiceException("ACCESS_DENIED", 403,
                "Access denied to transaction data for: " + identifier);
    }

    public static TransactionServiceException stateConflict(String identifier, String errorBody) {
        return new TransactionServiceException("TRANSACTION_CONFLICT", 409,
                "Transaction state conflict for [" + identifier + "]: " + errorBody);
    }

    public static TransactionServiceException operationFailed(String operation, String identifier, String errorBody) {
        return new TransactionServiceException(operation, 500,
                String.format("Failed execution of %s for [%s] - %s", operation, identifier, errorBody));
    }

    public static TransactionServiceException timeout(String operation) {
        return new TransactionServiceException(operation, 408,
                "Transaction service timeout for operation: " + operation);
    }

    public static TransactionServiceException unavailable(String operation) {
        return new TransactionServiceException(operation, 503,
                "Transaction service unavailable for operation: " + operation);
    }
}
