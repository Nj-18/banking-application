package com.banking.demo.repositories;

import com.banking.demo.entities.BankAccount;
import com.banking.demo.entities.Transaction;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccount(BankAccount account);

    List<Transaction> findByBankAccount(BankAccount account, Sort sort);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.bankAccount = :account
              AND (:fromDate IS NULL OR t.transactionDate >= :fromDate)
              AND (:toDate IS NULL OR t.transactionDate <= :toDate)
            """)
    List<Transaction> findByBankAccountAndDateRange(
            @Param("account") BankAccount account,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Sort sort);
}
