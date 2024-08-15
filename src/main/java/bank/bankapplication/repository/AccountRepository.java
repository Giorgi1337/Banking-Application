package bank.bankapplication.repository;

import bank.bankapplication.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByAccountHolderName(String accountHolderName);
    Page<Account> findByAccountHolderNameContaining(String accountHolderName, Pageable pageable);
}
