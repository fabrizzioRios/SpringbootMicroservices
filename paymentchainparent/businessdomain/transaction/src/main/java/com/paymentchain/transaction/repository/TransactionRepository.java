package com.paymentchain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.paymentchain.transaction.entities.Transaction;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository <Transaction, Long>{
    @Query("SELECT t FROM Transaction t WHERE t.accountIban = ?1")
    public Transaction findByIban(String consumerIban);
}
