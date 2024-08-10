package bank.bankapplication.repository;

import bank.bankapplication.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByAccountNumber_ShouldReturnAccount() {
        // Use a date that ensures the account holder is at least 18 years old
        LocalDate validDateOfBirth = LocalDate.now().minusYears(20);

        Account account = new Account();
        account.setAccountNumber("123456");
        account.setAccountHolderName("John Doe");
        account.setDateOfBirth(validDateOfBirth);

        accountRepository.save(account);

        Optional<Account> foundAccount = accountRepository.findByAccountNumber("123456");
        assertTrue(foundAccount.isPresent());
        assertEquals("John Doe", foundAccount.get().getAccountHolderName());
    }

}
