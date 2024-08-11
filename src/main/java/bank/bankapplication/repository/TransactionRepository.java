package bank.bankapplication.repository;

import bank.bankapplication.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByFromAccountHolderName(String fromAccountHolderName);
    List<Transaction> findByToAccountHolderName(String toAccountHolderName);
}