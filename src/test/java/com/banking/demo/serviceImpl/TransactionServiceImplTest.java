package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.TransactionResponseDTO;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Transaction;
import com.banking.demo.exceptions.AccountNotFoundException;
import com.banking.demo.exceptions.InvalidDateRangeException;
import com.banking.demo.exceptions.InvalidSortException;
import com.banking.demo.repositories.BankAccountRepository;
import com.banking.demo.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private BankAccount account;

    @BeforeEach
    void setUp() {
        account = new BankAccount();
        account.setAccountNumber("ACC123");
    }

    @Test
    void getTransactions_defaultsToDescendingSort() {
        when(bankAccountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByBankAccountAndDateRange(
                eq(account), isNull(), isNull(), org.mockito.ArgumentMatchers.any(Sort.class)))
                .thenReturn(List.of(buildTransaction("TXN1", LocalDateTime.of(2024, 6, 1, 10, 0))));

        List<TransactionResponseDTO> result =
                transactionService.getTransactions("ACC123", null, null, "desc");

        assertEquals(1, result.size());
        assertEquals("TXN1", result.get(0).getTransactionReference());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(transactionRepository).findByBankAccountAndDateRange(
                eq(account), isNull(), isNull(), sortCaptor.capture());

        Sort.Order order = sortCaptor.getValue().getOrderFor("transactionDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getTransactions_appliesAscendingSortAndDateFilter() {
        LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59);

        when(bankAccountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));
        when(transactionRepository.findByBankAccountAndDateRange(
                eq(account), eq(from), eq(to), org.mockito.ArgumentMatchers.any(Sort.class)))
                .thenReturn(List.of(
                        buildTransaction("TXN1", LocalDateTime.of(2024, 2, 1, 10, 0)),
                        buildTransaction("TXN2", LocalDateTime.of(2024, 3, 1, 10, 0))));

        List<TransactionResponseDTO> result =
                transactionService.getTransactions("ACC123", from, to, "asc");

        assertEquals(2, result.size());

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(transactionRepository).findByBankAccountAndDateRange(
                eq(account), eq(from), eq(to), sortCaptor.capture());

        Sort.Order order = sortCaptor.getValue().getOrderFor("transactionDate");
        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void getTransactions_rejectsInvalidDateRange() {
        LocalDateTime from = LocalDateTime.of(2024, 12, 31, 0, 0);
        LocalDateTime to = LocalDateTime.of(2024, 1, 1, 0, 0);

        when(bankAccountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidDateRangeException.class, () ->
                transactionService.getTransactions("ACC123", from, to, "desc"));
    }

    @Test
    void getTransactions_rejectsInvalidSort() {
        when(bankAccountRepository.findByAccountNumber("ACC123"))
                .thenReturn(Optional.of(account));

        assertThrows(InvalidSortException.class, () ->
                transactionService.getTransactions("ACC123", null, null, "latest"));
    }

    @Test
    void getTransactions_throwsWhenAccountMissing() {
        when(bankAccountRepository.findByAccountNumber("MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                transactionService.getTransactions("MISSING", null, null, "desc"));
    }

    private Transaction buildTransaction(String reference, LocalDateTime date) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(reference);
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(100.0);
        transaction.setBalanceAfterTransaction(1000.0);
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(date);
        transaction.setBankAccount(account);
        return transaction;
    }
}
