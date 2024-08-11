package bank.bankapplication.service;

import bank.bankapplication.exception.AccountNotFoundException;
import bank.bankapplication.exception.InvalidAmountException;
import bank.bankapplication.exception.ValidationException;
import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.AccountRepository;
import bank.bankapplication.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final WithdrawalRepository withdrawalRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(String accountNumber, String accountHolderName, LocalDate dateOfBirth) {
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new ValidationException("Account with the same account number already exists.");
        }
        Account account = new Account();
        account.setAccountHolderName(accountHolderName);
        account.setAccountNumber(accountNumber);
        account.setDateOfBirth(dateOfBirth);
        return accountRepository.save(account);
    }

    public double deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be a positive number");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    public List<Withdrawal> getAllWithdrawals() {
        return withdrawalRepository.findAll();
    }

    public double withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be a positive number");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        if (account.getBalance() - amount < 0) {
            throw new InvalidAmountException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        // Save withdrawal history with Account reference
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAccount(account);
        withdrawal.setAmount(amount);
        withdrawalRepository.save(withdrawal);
        return account.getBalance();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional
    public Account updateAccount(Account updatedAccount) {
        Account existingAccount = accountRepository.findByAccountNumber(updatedAccount.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
        return accountRepository.save(existingAccount);
    }

    public void deleteAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        accountRepository.delete(account);
    }

    public double checkBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getBalance();
    }

    @Transactional
    public void transferByAccountNumbers(String fromAccountNumber, String toAccountNumber, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Transfer amount must be a positive number");
        }
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (fromAccount.getBalance() - amount < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

}
