package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.dtos.TransferRequestDTO;
import com.banking.demo.dtos.TransferResponseDTO;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Transaction;
import com.banking.demo.exceptions.*;
import com.banking.demo.mappers.TransactionMapper;
import com.banking.demo.repositories.BankAccountRepository;
import com.banking.demo.repositories.TransactionRepository;
import com.banking.demo.services.TransactionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  BankAccountRepository bankAccountRepository) {

        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public List<TransactionResponseDTO> getTransactions(String accountNumber) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + accountNumber));

        List<Transaction> transactions =
                transactionRepository.findByBankAccount(account);

        return transactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void saveTransaction(BankAccount account, String transactionReference, String transactionType, Double amount, String remarks) {

    }@Override
    @Transactional
    public TransferResponseDTO transferMoney(TransferRequestDTO request) {

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new SameAccountTransferException(
                    "Transfer between the same account is not allowed.");
        }

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Transfer amount must be greater than zero.");
        }

        BankAccount sender = bankAccountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException("Sender account not found."));

        BankAccount receiver = bankAccountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException("Receiver account not found."));

        if (!sender.getAccountStatus().equalsIgnoreCase("ACTIVE")) {
            throw new AccountInactiveException(
                    "Sender account is inactive.");
        }

        if (!receiver.getAccountStatus().equalsIgnoreCase("ACTIVE")) {
            throw new AccountInactiveException(
                    "Receiver account is inactive.");
        }

        if (sender.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance.");
        }

        // Update balances
        sender.setBalance(sender.getBalance() - request.getAmount());
        receiver.setBalance(receiver.getBalance() + request.getAmount());

        // Save updated accounts
        bankAccountRepository.save(sender);
        bankAccountRepository.save(receiver);

        // Common transaction reference
        String transactionReference = "TXN" + System.currentTimeMillis();

        // Save sender transaction
        saveTransaction(
                sender,
                transactionReference,
                "TRANSFER_DEBIT",
                request.getAmount(),
                request.getRemarks());

        // Save receiver transaction
        saveTransaction(
                receiver,
                transactionReference,
                "TRANSFER_CREDIT",
                request.getAmount(),
                request.getRemarks());

        // Prepare response
        TransferResponseDTO response = new TransferResponseDTO();

        response.setTransactionReference(transactionReference);
        response.setFromAccount(sender.getAccountNumber());
        response.setToAccount(receiver.getAccountNumber());
        response.setAmount(request.getAmount());
        response.setSenderBalance(sender.getBalance());
        response.setReceiverBalance(receiver.getBalance());
        response.setMessage("Money transferred successfully.");

        return response;
    }


}