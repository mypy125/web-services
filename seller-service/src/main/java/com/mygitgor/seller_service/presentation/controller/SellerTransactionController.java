package com.mygitgor.seller_service.presentation.controller;

import com.mygitgor.seller_service.application.dto.external.TransactionDto;
import com.mygitgor.seller_service.application.dto.external.TransactionStatisticsDto;
import com.mygitgor.seller_service.application.dto.request.CreateCommissionCommand;
import com.mygitgor.seller_service.application.dto.request.CreateRefundCommand;
import com.mygitgor.seller_service.application.dto.request.CreateSaleCommand;
import com.mygitgor.seller_service.application.service.SellerTransactionService;
import com.mygitgor.seller_service.shared.valueobject.id.OrderId;
import com.mygitgor.seller_service.shared.valueobject.id.SellerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerTransactionController {
    private final SellerTransactionService transactionService;

    @PostMapping("/{sellerId}/transactions/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionDto> createSaleTransaction(@PathVariable String sellerId,
                                                      @RequestBody CreateSaleCommand command
    ) {
        log.info("REST request to create sale transaction for seller: {}", sellerId);
        return transactionService.createSaleTransaction(new SellerId(sellerId), command);
    }

    @PostMapping("/{sellerId}/transactions/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionDto> createRefundTransaction(
            @PathVariable String sellerId,
            @RequestBody CreateRefundCommand command
    ) {
        log.info("REST request to create refund transaction for seller: {}", sellerId);
        return transactionService.createRefundTransaction(new SellerId(sellerId), command);
    }

    @PostMapping("/{sellerId}/transactions/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionDto> createCommissionTransaction(
            @PathVariable String sellerId,
            @RequestBody CreateCommissionCommand command
    ) {
        log.info("REST request to create commission transaction for seller: {}", sellerId);
        return transactionService.createCommissionTransaction(new SellerId(sellerId), command);
    }

    @PostMapping("/transactions/{transactionId}/complete")
    public Mono<TransactionDto> completeTransaction(@PathVariable String transactionId) {
        log.info("REST request to complete transaction: {}", transactionId);
        return transactionService.completeTransaction(transactionId);
    }

    @PostMapping("/transactions/{transactionId}/fail")
    public Mono<TransactionDto> failTransaction(
            @PathVariable String transactionId,
            @RequestParam String reason
    ) {
        log.info("REST request to fail transaction: {} due to: {}", transactionId, reason);
        return transactionService.failTransaction(transactionId, reason);
    }

    @PostMapping("/transactions/{transactionId}/refund")
    public Mono<TransactionDto> refundTransaction(@PathVariable String transactionId) {
        log.info("REST request to trigger refund process for transaction: {}", transactionId);
        return transactionService.refundTransaction(transactionId);
    }

    @GetMapping("/transactions/{transactionId}")
    public Mono<TransactionDto> getTransactionById(@PathVariable String transactionId) {
        log.debug("REST request to get transaction by ID: {}", transactionId);
        return transactionService.getTransactionById(transactionId);
    }

    @GetMapping("/transactions/by-order/{orderId}")
    public Mono<TransactionDto> getTransactionByOrderId(@PathVariable String orderId) {
        log.debug("REST request to get transaction by order ID: {}", orderId);
        return transactionService.getTransactionByOrderId(new OrderId(orderId));
    }

    @GetMapping("/{sellerId}/transactions")
    public Flux<TransactionDto> getTransactionsBySellerId(@PathVariable String sellerId,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("REST request to get transactions for seller: {}, page: {}, size: {}", sellerId, page, size);
        return transactionService.getTransactionsBySellerId(new SellerId(sellerId), page, size);
    }

    @GetMapping("/{sellerId}/transactions/filter")
    public Flux<TransactionDto> getTransactionsFiltered(@PathVariable String sellerId,
                                                        @RequestParam(required = false) String type,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("REST request to get filtered transactions for seller: {}, type: {}, status: {}", sellerId, type, status);
        SellerId id = new SellerId(sellerId);

        if (type != null && status != null) {
            return transactionService.getTransactionsBySellerIdAndTypeAndStatus(id, type, status, page, size);
        } else if (type != null) {
            return transactionService.getTransactionsBySellerIdAndType(id, type, page, size);
        } else if (status != null) {
            return transactionService.getTransactionsBySellerIdAndStatus(id, status, page, size);
        }
        return transactionService.getTransactionsBySellerId(id, page, size);
    }

    @GetMapping("/{sellerId}/transactions/date-range")
    public Flux<TransactionDto> getTransactionsByDateRange(@PathVariable String sellerId,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("REST request to get transactions by date for seller: {} between {} and {}", sellerId, startDate, endDate);
        return transactionService.getTransactionsByDateBetween(new SellerId(sellerId), startDate, endDate, page, size);
    }

    @GetMapping("/transactions/batch")
    public Flux<TransactionDto> getTransactionsByIds(@RequestParam List<String> ids) {
        log.debug("REST request to batch fetch transactions: {}", ids);
        return transactionService.getTransactionsByIds(ids);
    }

    @GetMapping("/transactions/by-reference/{referenceNumber}")
    public Mono<TransactionDto> getTransactionByReferenceNumber(@PathVariable String referenceNumber) {
        log.debug("REST request to get transaction by reference: {}", referenceNumber);
        return transactionService.getTransactionByReferenceNumber(referenceNumber);
    }

    @GetMapping("/{sellerId}/transactions/statistics")
    public Mono<TransactionStatisticsDto> getTransactionStatistics(@PathVariable String sellerId,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.debug("REST request to get statistics for seller: {}", sellerId);
        SellerId id = new SellerId(sellerId);

        if (startDate != null && endDate != null) {
            return transactionService.getTransactionStatistics(id, startDate, endDate);
        }
        return transactionService.getTransactionStatistics(id);
    }
}
