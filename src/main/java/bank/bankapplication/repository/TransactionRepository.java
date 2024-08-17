package bank.bankapplication.repository;

import bank.bankapplication.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByFromAccountHolderName(String fromAccountHolderName);
    List<Transaction> findByToAccountHolderName(String toAccountHolderName);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(:fromDate IS NULL OR t.transactionDate >= :fromDate) AND " +
            "(:toDate IS NULL OR t.transactionDate <= :toDate) AND " +
            "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
            "(:transactionType IS NULL OR t.transactionType = :transactionType)")
    Page<Transaction> findTransactions(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minAmount") double minAmount,
            @Param("maxAmount") double maxAmount,
            @Param("transactionType") String transactionType,
            Pageable pageable);

}