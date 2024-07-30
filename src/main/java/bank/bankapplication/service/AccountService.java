package bank.bankapplication.service;

import bank.bankapplication.model.Account;
import bank.bankapplication.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account createAccount(String accountNumber, String accountHolderName, double initialDeposit) {
        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountHolderName(accountHolderName);
        account.setBalance(initialDeposit);
        return accountRepository.save(account);
    }

    public double deposit(String accountNumber, double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    public double withdraw(String accountNumber, double amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.getBalance() - amount < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    public double checkBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return account.getBalance();
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        withdraw(fromAccountNumber, amount);
        deposit(toAccountNumber, amount);
    }

}
