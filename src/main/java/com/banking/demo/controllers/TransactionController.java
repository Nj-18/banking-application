package com.banking.demo.controllers;

import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.TransferResponseDTO;
import com.banking.demo.services.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountNumber}")
    public List<TransactionResponseDTO> getTransactions(
            @PathVariable String accountNumber) {

        return transactionService.getTransactions(accountNumber);
    }

    @PostMapping("/transfer")
    public TransferResponseDTO transferDetails(@RequestBody TransferRequestDTO requestDTO)
    {
        return transactionService.transferMoney(requestDTO);
    }
}