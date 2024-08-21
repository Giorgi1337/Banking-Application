package bank.bankapplication.controller;


import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class BaseController {

    private final AccountService accountService;

    protected void addCommonModelAttributes(Model model, Page<?> page, String name) {
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("name", name);
    }

    protected void addErrorMessage(Model model, String errorMessage) {
        model.addAttribute("errorMessage", errorMessage);
    }

    protected void addSuccessMessage(Model model, String message) {
        model.addAttribute("message", message);
    }

    protected void populateAccountDetails(String accountNumber, Model model) throws AccountNotFoundException {
        Account account = accountService.getAccountByNumber(accountNumber);
        model.addAttribute("accountNumber", account.getAccountNumber());
        model.addAttribute("accountHolderName", account.getAccountHolderName());
        model.addAttribute("balance", account.getBalance());
    }

    protected void updateAccountDetails(Account existingAccount, Account updatedAccount) {
        existingAccount.setAccountHolderName(updatedAccount.getAccountHolderName());
        existingAccount.setEmailAddress(updatedAccount.getEmailAddress());
        existingAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
        existingAccount.setAddress(updatedAccount.getAddress());
        existingAccount.setAccountType(updatedAccount.getAccountType());
        existingAccount.setStatus(updatedAccount.getStatus());
    }

    protected String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }

    protected List<Transaction> getRecentTransactions(String accountHolderName) {
        return accountService.getTransactionsByAccountHolderName(accountHolderName)
                .stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .limit(3)
                .collect(Collectors.toList());
    }

    protected LocalDate parseDate(String date) {
        return date != null && !date.isEmpty() ? LocalDate.parse(date) : null;
    }
}
