package bank.bankapplication.service;

import bank.bankapplication.exception.InvalidAmountException;
import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.repository.AccountRepository;
import bank.bankapplication.repository.TransactionRepository;
import bank.bankapplication.repository.WithdrawalRepository;
import bank.bankapplication.utils.PdfUtils;
import com.itextpdf.text.*;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final WithdrawalRepository withdrawalRepository;

    private final TransactionRepository transactionRepository;

    public Page<Account> getAccountsPaginated(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("accountHolderName").ascending());
        return accountRepository.findByAccountHolderNameContaining(name, pageable);
    }

    public Account createAccount(String accountNumber, String accountHolderName, LocalDate dateOfBirth,
                                 String emailAddress, String phoneNumber, String address,
                                 String accountType, String status) {

        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new ValidationException("Account with the same account number already exists.");
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setAccountHolderName(accountHolderName);
        account.setDateOfBirth(dateOfBirth);
        account.setEmailAddress(emailAddress);
        account.setPhoneNumber(phoneNumber);
        account.setAddress(address);
        account.setAccountType(accountType);
        account.setStatus(status);
        account.setBalance(0.0); // Initial balance

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

    public Page<Transaction> getTransactionsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return transactionRepository.findAll(pageable);
    }

    public Page<Withdrawal> getWithdrawalsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("account.accountHolderName").ascending());
        return withdrawalRepository.findAll(pageable);
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

    public Account getAccountByNumber(String accountNumber) {
        return findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for number: " + accountNumber));
    }

    @Transactional
    public Account updateAccount(Account updatedAccount) throws AccountNotFoundException {
        Account existingAccount = accountRepository.findByAccountNumber(updatedAccount.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));

        if (updatedAccount.getAccountHolderName() != null) {
            existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
        }
        if (updatedAccount.getEmailAddress() != null) {
            existingAccount.setEmailAddress(updatedAccount.getEmailAddress());
        }
        if (updatedAccount.getPhoneNumber() != null) {
            existingAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
        }
        if (updatedAccount.getAddress() != null) {
            existingAccount.setAddress(updatedAccount.getAddress());
        }
        if (updatedAccount.getAccountType() != null) {
            existingAccount.setAccountType(updatedAccount.getAccountType());
        }
        if (updatedAccount.getStatus() != null) {
            existingAccount.setStatus(updatedAccount.getStatus());
        }

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
        Transaction transaction = new Transaction();
        transaction.setFromAccountHolderName(fromAccount.getAccountHolderName());
        transaction.setToAccountHolderName(toAccount.getAccountHolderName());
        transaction.setTransactionType("Transfer");
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }

    public Page<Transaction> searchTransactionsPaginated(
            LocalDate fromDate,
            LocalDate toDate,
            Double minAmount,
            Double maxAmount,
            String transactionType,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        return transactionRepository.findTransactions(
                fromDate, toDate, minAmount, maxAmount, transactionType, pageable);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public ResponseEntity<InputStreamResource> generateWithdrawalsPdf(int page, int size) throws DocumentException, IOException {
        // Fetch only the withdrawals for the specified page and size
        List<Withdrawal> withdrawals = withdrawalRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = withdrawals.stream()
                .map(w -> List.of(
                        w.getAccount().getAccountHolderName(),
                        String.format("$%.2f", w.getAmount()),
                        w.getWithdrawDate().toString()
                ))
                .collect(Collectors.toList());

        String[] headers = {"Account Holder Name", "Amount", "Date"};
        String fileName = "withdrawals_report_page_" + page + ".pdf";

        InputStreamResource pdf = PdfUtils.generatePdf("Withdrawals Report", headers, rows, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public ResponseEntity<InputStreamResource> generateTransactionsPdf(int page, int size) throws DocumentException, IOException {
        // Fetch transactions for the specified page and size
        List<Transaction> transactions = transactionRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = transactions.stream()
                .map(t -> List.of(
                        t.getFromAccountHolderName() != null ? t.getFromAccountHolderName() : "N/A",
                        t.getToAccountHolderName(),
                        t.getTransactionType(),
                        String.format("%.1f", t.getAmount()),
                        t.getTransactionDate().toString()
                ))
                .collect(Collectors.toList());

        String[] headers = {"From Account Holder", "To Account Holder", "Type", "Amount", "Date"};
        String fileName = "transactions_report_page_" + page + ".pdf";

        InputStreamResource pdf = PdfUtils.generatePdf("Transactions Report", headers, rows, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
