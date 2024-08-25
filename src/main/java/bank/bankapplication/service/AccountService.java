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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final WithdrawalRepository withdrawalRepository;

    private final TransactionRepository transactionRepository;

    public Page<Account> getAccountsPaginated(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("accountHolderName").ascending());
        return accountRepository.findByAccountHolderNameContainingIgnoreCase(name, pageable);
    }

    public void createAccount(String accountNumber, String accountHolderName, LocalDate dateOfBirth,
                              String emailAddress, String phoneNumber, String address,
                              String accountType, String status) {

        validateAccountNumber(accountNumber);

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

        accountRepository.save(account);
    }

    @Transactional
    public double deposit(String accountNumber, double amount) throws AccountNotFoundException {
        Account account = getAccountOrThrow(accountNumber);
        validateAmount(amount);

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        recordTransaction("N/A", account.getAccountHolderName(), "Deposit", amount);
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
        Account account = getAccountOrThrow(accountNumber);
        validateAmount(amount);
        validateSufficientFunds(account, amount);

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        recordTransaction(account.getAccountHolderName(), "N/A", "Withdrawal", amount);
        saveWithdrawal(account, amount);

        return account.getBalance();
    }

    public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
        return getAccountOrThrow(accountNumber);
    }

    @Transactional
    public void updateAccount(Account updatedAccount) throws AccountNotFoundException {
        Account existingAccount = getAccountOrThrow(updatedAccount.getAccountNumber());

        updateNonNullFields(existingAccount, updatedAccount);
        accountRepository.save(existingAccount);
    }

    @Transactional
    public void deleteAccount(String accountNumber) throws AccountNotFoundException {
        Account account = getAccountOrThrow(accountNumber);
        withdrawalRepository.deleteAll(withdrawalRepository.findByAccount(account));
        accountRepository.delete(account);
    }

    public double checkBalance(String accountNumber) throws AccountNotFoundException {
        Account account = getAccountOrThrow(accountNumber);
        return account.getBalance();
    }

    @Transactional
    public void transferByAccountNumbers(String fromAccountNumber, String toAccountNumber, double amount) throws AccountNotFoundException {
        validateAmount(amount);
        Account fromAccount = getAccountOrThrow(fromAccountNumber);
        Account toAccount = getAccountOrThrow(toAccountNumber);

        if (fromAccount.equals(toAccount)) {
            throw new InvalidAmountException("Cannot transfer to the same account.");
        }
        validateSufficientFunds(fromAccount, amount);

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        recordTransaction(fromAccount.getAccountHolderName(), toAccount.getAccountHolderName(), "Transfer", amount);
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
        return transactionRepository.findTransactions(fromDate, toDate, minAmount, maxAmount, transactionType, pageable);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public ResponseEntity<InputStreamResource> generateTransactionsPdfByHolder(String accountHolderName) throws DocumentException, IOException {
        List<Transaction> transactions = getTransactionsByAccountHolderName(accountHolderName);
        List<List<String>> rows = mapTransactionsToRows(transactions);

        return generatePdfResponse("Transactions for " + accountHolderName,
                new String[]{"From", "To", "Type", "Amount", "Date"},
                rows,
                "transactions_" + accountHolderName + ".pdf");
    }

    public ResponseEntity<InputStreamResource> generateWithdrawalsPdf(int page, int size) throws DocumentException, IOException {
        List<Withdrawal> withdrawals = withdrawalRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = withdrawals.stream()
                .map(w -> List.of(
                        w.getAccount().getAccountHolderName(),
                        String.format("$%.2f", w.getAmount()),
                        w.getWithdrawDate().toString()
                ))
                .collect(Collectors.toList());

        return generatePdfResponse("Withdrawals Report",
                new String[]{"Account Holder Name", "Amount", "Date"},
                rows,
                "withdrawals_report_page_" + page + ".pdf");
    }

    public ResponseEntity<InputStreamResource> generateTransactionsPdf(int page, int size) throws DocumentException, IOException {
        List<Transaction> transactions = transactionRepository.findAll(PageRequest.of(page, size)).getContent();
        List<List<String>> rows = mapTransactionsToRows(transactions);

        return generatePdfResponse("Transactions Report",
                new String[]{"From Account Holder", "To Account Holder", "Type", "Amount", "Date"},
                rows,
                "transactions_report_page_" + page + ".pdf");
    }

    private void validateAccountNumber(String accountNumber) {
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            throw new ValidationException("Account with the same account number already exists.");
        }
    }

    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be a positive number.");
        }
    }

    private void validateSufficientFunds(Account account, double amount) {
        if (account.getBalance() - amount < 0) {
            throw new InvalidAmountException("Insufficient funds.");
        }
    }

    private Account getAccountOrThrow(String accountNumber) throws AccountNotFoundException {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found."));
    }

    private void recordTransaction(String from, String to, String type, double amount) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountHolderName(from);
        transaction.setToAccountHolderName(to);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
    }

    private void saveWithdrawal(Account account, double amount) {
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setAccount(account);
        withdrawal.setAmount(amount);
        withdrawalRepository.save(withdrawal);
    }

    private void updateNonNullFields(Account existingAccount, Account updatedAccount) {
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
    }

    private List<List<String>> mapTransactionsToRows(List<Transaction> transactions) {
        return transactions.stream()
                .map(t -> List.of(
                        t.getFromAccountHolderName() != null ? t.getFromAccountHolderName() : "N/A",
                        t.getToAccountHolderName(),
                        t.getTransactionType(),
                        String.format("%.1f", t.getAmount()),
                        t.getTransactionDate().toString()
                ))
                .collect(Collectors.toList());
    }

    private ResponseEntity<InputStreamResource> generatePdfResponse(String title, String[] headers, List<List<String>> rows, String fileName) throws DocumentException, IOException {
        InputStreamResource pdfStream = PdfUtils.generatePdf(title, headers, rows, fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfStream);
    }
}
