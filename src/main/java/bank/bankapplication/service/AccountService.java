package bank.bankapplication.service;

import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.AccountRepository;
import bank.bankapplication.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Account createAccount(String accountNumber, String accountHolderName, double initialDeposit) {
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new RuntimeException("Account with the same account number already exists.");
        }
        Account account = new Account();
        account.setAccountHolderName(accountHolderName);
        account.setAccountNumber(accountNumber);
      //  account.setBalance(initialDeposit);
        return accountRepository.save(account);
    }

    public double deposit(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Deposit amount must be a positive number");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    public List<Withdrawal> getAllWithdrawals() {
        return withdrawalRepository.findAll();
    }

    public double withdraw(String accountNumber, double amount) {
        if (amount <= 0) {
            throw new RuntimeException("Withdrawal amount must be a positive number");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.getBalance() - amount < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        // Save withdrawal history
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAccountNumber(accountNumber);
        withdrawal.setAmount(amount);
        withdrawalRepository.save(withdrawal);


        return account.getBalance();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

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

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
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
