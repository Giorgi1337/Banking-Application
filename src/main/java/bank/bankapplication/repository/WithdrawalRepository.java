package bank.bankapplication.repository;

import bank.bankapplication.model.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Integer> {
    List<Withdrawal> findByAccountNumber(String accountNumber);
}
