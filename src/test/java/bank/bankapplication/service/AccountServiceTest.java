package bank.bankapplication.service;

import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.AccountRepository;
import bank.bankapplication.repository.WithdrawalRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WithdrawalRepository withdrawalRepository;

    @InjectMocks
    private AccountService accountService;

    public AccountServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_ShouldReturnAccount() {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setAccountHolderName("John Doe");
        account.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account createdAccount = accountService.createAccount("123456", "John Doe", LocalDate.of(2000, 1, 1));
        assertNotNull(createdAccount);
        assertEquals("123456", createdAccount.getAccountNumber());
    }

    @Test
    void deposit_ShouldIncreaseBalance() {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(100.0);

        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        double newBalance = accountService.deposit("123456", 50.0);
        assertEquals(150.0, newBalance);
    }

    @Test
    void withdraw_ShouldDecreaseBalance() {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setBalance(200.0);

        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(withdrawalRepository.save(any(Withdrawal.class))).thenReturn(new Withdrawal());

        double newBalance = accountService.withdraw("123456", 100.0);
        assertEquals(100.0, newBalance);
    }

    @Test
    void transferByAccountNumbers_ShouldTransferFunds() {
        // Create test accounts with account numbers
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("123456");
        fromAccount.setAccountHolderName("John Doe");
        fromAccount.setBalance(200.0);

        Account toAccount = new Account();
        toAccount.setAccountNumber("654321");
        toAccount.setAccountHolderName("Jane Doe");
        toAccount.setBalance(300.0);

        // Mock repository behavior
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.of(toAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        // Perform the transfer
        accountService.transferByAccountNumbers("123456", "654321", 50.0);

        // Verify the balance after transfer
        assertEquals(150.0, fromAccount.getBalance());
        assertEquals(350.0, toAccount.getBalance());

        // Verify that the save method was called
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
    }
}
