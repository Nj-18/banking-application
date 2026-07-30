package com.banking.demo.serviceImpl;

import com.banking.demo.dtos.*;
import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Customer;
import com.banking.demo.entities.Transaction;
import com.banking.demo.exceptions.AccountNotFoundException;
import com.banking.demo.exceptions.CustomerNotFoundException;
import com.banking.demo.exceptions.InsufficientBalanceException;
import com.banking.demo.exceptions.InvalidAmountException;
import com.banking.demo.repositories.BankAccountRepository;
import com.banking.demo.repositories.CustomerRepository;
import com.banking.demo.repositories.TransactionRepository;
import com.banking.demo.services.BankAccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private final TransactionRepository transactionRepository;
private final BankAccountRepository bankAccountRepository;
private final CustomerRepository customerRepository;

    public BankAccountServiceImpl(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public BankAccount createAccount(CreateBankAccountRequestDTO request) {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountType(request.getAccountType());
        bankAccount.setBalance(request.getOpeningBalance());
        bankAccount.setAccountNumber("ACC" + System.currentTimeMillis());
        bankAccount.setAccountStatus("ACTIVE");

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with id : " + request.getCustomerId()));

        bankAccount.setCustomer(customer);

        return bankAccountRepository.save(bankAccount);

        //bankAccount.setCustomer();
        //Customer customer = customerRepository.findById(request.getCustomerId())
                // .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public DepositResponseDTO depositMoney(DepositRequestDTO request) {

        // 1. Find Account
        BankAccount account = bankAccountRepository
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + request.getAccountNumber()));

        // 2. Validate
        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Deposit amount must be greater than zero.");
        }

        // 3. Update Balance
        Double previousBalance = account.getBalance();
        Double updatedBalance = previousBalance + request.getAmount();

        account.setBalance(updatedBalance);

        // 4. Save updated account
        bankAccountRepository.save(account);

        // 5. Create transaction
        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN" + System.currentTimeMillis());
        transaction.setTransactionType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfterTransaction(updatedBalance);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("Amount deposited successfully");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBankAccount(account);

        // 6. Save transaction
        transactionRepository.save(transaction);


        DepositResponseDTO response = new DepositResponseDTO();
        response.setAccountNumber(account.getAccountNumber());
        response.setDepositedAmount(request.getAmount());
        response.setPreviousBalance(previousBalance);
        response.setUpdatedBalance(updatedBalance);
        response.setMessage("Amount deposited successfully.");

        return response;
    }

    @Override
    public WithdrawResponseDTO withdrawMoney(WithdrawRequestDTO request) {

        BankAccount account = bankAccountRepository
                .findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with account number : "
                                        + request.getAccountNumber()));

        if (request.getAmount() <= 0) {
            throw new InvalidAmountException(
                    "Withdrawal amount must be greater than zero.");
        }

        if (!account.getAccountStatus().equalsIgnoreCase("ACTIVE")) {
            throw new RuntimeException("Account is not active.");
        }

        if (account.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. Available balance is ₹"
                            + account.getBalance());
        }

        Double previousBalance = account.getBalance();

        Double updatedBalance = previousBalance - request.getAmount();

        account.setBalance(updatedBalance);

        bankAccountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN" + System.currentTimeMillis());
        transaction.setTransactionType("WITHDRAW");
        transaction.setAmount(request.getAmount());
        transaction.setBalanceAfterTransaction(updatedBalance);
        transaction.setStatus("SUCCESS");
        transaction.setRemarks("Amount withdrawn successfully");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBankAccount(account);

        // 6. Save transaction
        transactionRepository.save(transaction);

        // 7. Return response DTO
        WithdrawResponseDTO response = new WithdrawResponseDTO();

        response.setAccountNumber(account.getAccountNumber());
        response.setWithdrawnAmount(request.getAmount());
        response.setPreviousBalance(previousBalance);
        response.setUpdatedBalance(updatedBalance);
        response.setMessage("Amount withdrawn successfully.");

        return response;
    }

}
