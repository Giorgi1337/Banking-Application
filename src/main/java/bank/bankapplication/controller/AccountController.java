package bank.bankapplication.controller;

import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/accounts")
    public String viewAllAccounts(Model model) {
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "account/viewAccounts";
    }

    @GetMapping("/create")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "account/createAccount";
    }

    @PostMapping("/create")
    public String createAccount(@Valid @ModelAttribute Account account, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "account/createAccount";
        }

        try {
            accountService.createAccount(account.getAccountNumber(), account.getAccountHolderName(), account.getDateOfBirth());
            return "redirect:/accounts";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error creating account: " + e.getMessage());
            return "account/createAccount";
        }
    }

    @GetMapping("/update/{accountNumber}")
    public String showUpdateForm(@PathVariable String accountNumber, Model model) {
        Account account = getAccountByNumber(accountNumber);
        model.addAttribute("account", account);
        return "account/updateAccount";
    }

    @PostMapping("/update")
    public String updateAccount(@Valid @ModelAttribute Account account, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "account/updateAccount";
        }
        try {
            accountService.updateAccount(account);
            model.addAttribute("message", "Account updated successfully");
            return "redirect:/accounts";
        } catch (RuntimeException | AccountNotFoundException e) {
            model.addAttribute("errorMessage", "Error updating account: " + e.getMessage());
            return "account/updateAccount";
        }
    }


    @GetMapping("/delete/{accountNumber}")
    public String deleteAccount(@PathVariable String accountNumber, Model model) {
        try {
            accountService.deleteAccount(accountNumber);
            return "redirect:/accounts";
        } catch (RuntimeException | AccountNotFoundException e) {
            model.addAttribute("errorMessage", "Error deleting account: " + e.getMessage());
            return "redirect:/accounts";
        }
    }

    @GetMapping("/deposit/{accountNumber}")
    public String showDepositForm(@PathVariable String accountNumber, Model model) {
        Account account = getAccountByNumber(accountNumber);
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("accountHolderName", account.getAccountHolderName());
        return "transaction/deposit";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam double amount,
            Model model) {
        try {
            double balance = accountService.deposit(accountNumber, amount);
            model.addAttribute("message", "Deposit successful. New balance: " + balance);
            return "transaction/depResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            model.addAttribute("errorMessage", "Error during deposit: " + e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "transaction/deposit";
        }
    }

    @GetMapping("/withdraw/{accountNumber}")
    public String showWithdrawForm(@PathVariable String accountNumber, Model model) {
        Account account = getAccountByNumber(accountNumber);
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("accountHolderName", account.getAccountHolderName());
        model.addAttribute("balance", account.getBalance());

        return "withdraw/withdraw";
    }


    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam double amount,
            Model model) {
        try {
            double balance = accountService.withdraw(accountNumber, amount);
            model.addAttribute("message", "Withdrawal successful. New balance: " + balance);
            return "withdraw/withdrawResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            model.addAttribute("errorMessage", "Error during withdrawal: " + e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "withdraw/withdraw";
        }
    }

    @GetMapping("/withdrawals")
    public String showWithdrawals(Model model) {
        List<Withdrawal> withdrawals = accountService.getAllWithdrawals();
        model.addAttribute("withdrawals", withdrawals);
        return "withdraw/withdrawals";
    }


    @GetMapping("/balance")
    public String showBalanceForm() {
        return "balance/checkBalance";
    }

    @PostMapping("/balance")
    public String checkBalance(@RequestParam String accountNumber, Model model) throws AccountNotFoundException {
        double balance = accountService.checkBalance(accountNumber);
        model.addAttribute("message", "Current balance: " + balance);
        return "balance/balanceResult";
    }

    @GetMapping("/transfer/{fromAccountNumber}")
    public String showTransferForm(@PathVariable String fromAccountNumber, Model model) {
        Account fromAccount = getAccountByNumber(fromAccountNumber);
        List<Account> accounts = accountService.getAllAccounts();
        accounts.remove(fromAccount);

        model.addAttribute("fromAccountNumber", fromAccountNumber);
        model.addAttribute("fromAccountHolderName", fromAccount.getAccountHolderName());
        model.addAttribute("fromAccountBalance", fromAccount.getBalance());
        model.addAttribute("accounts", accounts);

        return "transaction/transfer";
    }


    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam double amount,
            Model model) {
        try {
            accountService.transferByAccountNumbers(fromAccountNumber, toAccountNumber, amount);
            model.addAttribute("message", "Transfer successful");
            return "transaction/transferResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            model.addAttribute("errorMessage", "Error during transfer: " + e.getMessage());
            return "transaction/transfer";
        }
    }

    @GetMapping("/profile/{accountNumber}")
    public String showProfile(@PathVariable String accountNumber, Model model) {
        Account account = getAccountByNumber(accountNumber);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCreatedDate = account.getCreatedDate().format(formatter);

        List<Transaction> recentTransactions = accountService.getTransactionsByAccountHolderName(account.getAccountHolderName())
                .stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate())) // Sort by date descending
                .limit(3) // Get the last 3 transactions
                .collect(Collectors.toList());

        model.addAttribute("account", account);
        model.addAttribute("formattedCreatedDate", formattedCreatedDate);
        model.addAttribute("recentTransactions", recentTransactions);

        return "account/profile";
    }

    @GetMapping("/transactions/{accountHolderName}")
    public String viewTransactionsByHolder(@PathVariable String accountHolderName, Model model) {
        List<Transaction> transactions = accountService.getTransactionsByAccountHolderName(accountHolderName);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountHolderName", accountHolderName);
        return "transaction/viewTransactionsByHolder";
    }

    @GetMapping("/transactions")
    public String viewAllTransactions(Model model) {
        List<Transaction> transactions = accountService.getAllTransactions();
        model.addAttribute("transactions", transactions);

        List<String> transactionTypes = List.of("Deposit", "Withdrawal", "Transfer");
        model.addAttribute("transactionTypes", transactionTypes);

        return "transaction/viewTransactions";
    }

    @GetMapping("/transactions/search")
    public String searchTransactions(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String transactionType,
            Model model) {

        List<Transaction> transactions = accountService.searchTransactions(
                fromDate != null ? LocalDate.parse(fromDate) : null,
                toDate != null ? LocalDate.parse(toDate) : null,
                minAmount,
                maxAmount,
                transactionType
        );

        model.addAttribute("transactions", transactions);
        return "transaction/viewTransactions";
    }

    private Account getAccountByNumber(String accountNumber) {
        return accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for number: " + accountNumber));
    }
}
