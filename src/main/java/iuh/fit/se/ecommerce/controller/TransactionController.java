package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.response.TransactionResponse;
import iuh.fit.se.ecommerce.dto.response.TransactionSummaryResponse;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
import iuh.fit.se.ecommerce.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.getAllTransactions(
                type, status, startDate, endDate, pageable
        );
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        TransactionSummaryResponse summary = transactionService.getTransactionSummary(
                type, status, startDate, endDate
        );
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionDetail(
            @PathVariable Long transactionId
    ) {
        TransactionResponse transaction = transactionService.getTransactionDetail(transactionId);
        return ResponseEntity.ok(transaction);
    }
}

