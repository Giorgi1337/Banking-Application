package bank.bankapplication.repository;

import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Integer> {
    List<Withdrawal> findByAccount(Account account);
    Page<Withdrawal> findAll(Pageable pageable);
}
