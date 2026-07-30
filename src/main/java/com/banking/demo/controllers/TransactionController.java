package com.banking.demo.controllers;

import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.TransferResponseDTO;
import com.banking.demo.services.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Fetch transactions for an account with optional date filtering and sorting.
     *
     * Examples:
     * GET /api/transactions/{accountNumber}
     * GET /api/transactions/{accountNumber}?sort=asc
     * GET /api/transactions/{accountNumber}?fromDate=2024-01-01T00:00:00&toDate=2024-12-31T23:59:59
     * GET /api/transactions/{accountNumber}?fromDate=2024-01-01T00:00:00&toDate=2024-06-30T23:59:59&sort=desc
     */
    @GetMapping("/{accountNumber}")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,
            @RequestParam(required = false, defaultValue = "desc")
            String sort) {

        return transactionService.getTransactions(
                accountNumber, fromDate, toDate, sort);
    }

    @PostMapping("/transfer")
    public TransferResponseDTO transferDetails(@RequestBody TransferRequestDTO requestDTO)
    {
        return transactionService.transferMoney(requestDTO);
    }
}
