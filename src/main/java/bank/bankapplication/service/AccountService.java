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

    private final WithdrawalService withdrawalService;

    private final TransactionService transactionService;

    public Page<Account> getAccountsPaginated(int page, int size, String name) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("accountHolderName").ascending());
        return accountRepository.findByAccountHolderNameContainingIgnoreCase(name, pageable);
    }

    @Transactional
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

        transactionService.recordTransaction("N/A", account.getAccountHolderName(), "Deposit", amount);
        return account.getBalance();
    }

    @Transactional
    public double withdraw(String accountNumber, double amount) throws AccountNotFoundException {
        Account account = getAccountOrThrow(accountNumber);
        validateAmount(amount);
        validateSufficientFunds(account, amount);

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        transactionService.recordTransaction(account.getAccountHolderName(), "N/A", "Withdrawal", amount);
        withdrawalService.saveWithdrawal(account, amount);

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
        withdrawalService.deleteWithdrawalsByAccount(account);
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

        transactionService.recordTransaction(fromAccount.getAccountHolderName(), toAccount.getAccountHolderName(), "Transfer", amount);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
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
}
