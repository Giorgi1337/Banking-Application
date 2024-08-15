package bank.bankapplication.service;

import bank.bankapplication.exception.InvalidAmountException;
import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.AccountRepository;
import bank.bankapplication.repository.TransactionRepository;
import bank.bankapplication.repository.WithdrawalRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final WithdrawalRepository withdrawalRepository;

    private final TransactionRepository transactionRepository;

    public Page<Account> getAccountsPaginated(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size);
        return accountRepository.findByAccountHolderNameContaining(name, pageable);
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

    @Transactional
    public double deposit(String accountNumber, double amount) throws AccountNotFoundException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be a positive number.");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setFromAccountHolderName("N/A");
        transaction.setToAccountHolderName(account.getAccountHolderName());
        transaction.setTransactionType("Deposit");
        transaction.setAmount(amount);
        transactionRepository.save(transaction);

        return account.getBalance();
    }

    public List<Transaction> getTransactionsByAccountHolderName(String accountHolderName) {
        List<Transaction> fromTransactions = transactionRepository.findByFromAccountHolderName(accountHolderName);
        List<Transaction> toTransactions = transactionRepository.findByToAccountHolderName(accountHolderName);
        fromTransactions.addAll(toTransactions);
        return fromTransactions;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Withdrawal> getAllWithdrawals() {
        return withdrawalRepository.findAll();
    }

    @Transactional
    public double withdraw(String accountNumber, double amount) throws AccountNotFoundException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be a positive number.");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));

        if (account.getBalance() - amount < 0) {
            throw new InvalidAmountException("Insufficient funds.");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setFromAccountHolderName(account.getAccountHolderName());
        transaction.setToAccountHolderName("N/A");
        transaction.setTransactionType("Withdrawal");
        transaction.setAmount(amount);
        transactionRepository.save(transaction);

        // Save withdrawal history
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
    public Account updateAccount(Account updatedAccount) throws AccountNotFoundException {
        Account existingAccount = accountRepository.findByAccountNumber(updatedAccount.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));
        existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
        return accountRepository.save(existingAccount);
    }

    @Transactional
    public void deleteAccount(String accountNumber) throws AccountNotFoundException {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));

        withdrawalRepository.deleteAll(withdrawalRepository.findByAccount(account));

        accountRepository.delete(account);
    }

    public double checkBalance(String accountNumber) throws AccountNotFoundException {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));
        return account.getBalance();
    }

    @Transactional
    public void transferByAccountNumbers(String fromAccountNumber, String toAccountNumber, double amount) throws AccountNotFoundException {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be a positive number.");
        }
        Account fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Source account not found."));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Destination account not found."));

        if (fromAccount.equals(toAccount)) {
            throw new ValidationException("Cannot transfer to the same account.");
        }

        if (fromAccount.getBalance() - amount < 0) {
            throw new InvalidAmountException("Insufficient funds.");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record the transaction
        Transaction fromTransaction = new Transaction();
        fromTransaction.setFromAccountHolderName(fromAccount.getAccountHolderName());
        fromTransaction.setToAccountHolderName(toAccount.getAccountHolderName());
        fromTransaction.setTransactionType("Transfer Out");
        fromTransaction.setAmount(amount);
        transactionRepository.save(fromTransaction);

        Transaction toTransaction = new Transaction();
        toTransaction.setFromAccountHolderName(fromAccount.getAccountHolderName());
        toTransaction.setToAccountHolderName(toAccount.getAccountHolderName());
        toTransaction.setTransactionType("Transfer In");
        toTransaction.setAmount(amount);
        transactionRepository.save(toTransaction);
    }

    public List<Transaction> searchTransactions(LocalDate fromDate, LocalDate toDate, Double minAmount, Double maxAmount, String transactionType) {
        return transactionRepository.findTransactions(fromDate, toDate, minAmount, maxAmount, transactionType);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
