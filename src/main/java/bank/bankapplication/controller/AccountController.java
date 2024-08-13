package bank.bankapplication.controller;

import bank.bankapplication.exception.AccountNotFoundException;
import bank.bankapplication.exception.InvalidAmountException;
import bank.bankapplication.exception.ValidationException;
import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
            accountService.createAccount(
                    account.getAccountNumber(),
                    account.getAccountHolderName(),
                    account.getDateOfBirth()
            );
            return "redirect:/accounts";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "account/createAccount";
        }
    }

    @GetMapping("/update/{accountNumber}")
    public String showUpdateForm(@PathVariable String accountNumber, Model model) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        return "account/updateAccount";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute Account account, Model model) {
       try {
           accountService.updateAccount(account);
           model.addAttribute("message", "Account updated successfully");
       }catch(RuntimeException e) {
           model.addAttribute("message", "Error: " + e.getMessage());
       }
       return "redirect:/accounts";
    }

    @GetMapping("/delete/{accountNumber}")
    public String deleteAccount(@PathVariable String accountNumber, Model model) {
        try {
            accountService.deleteAccount(accountNumber);
        } catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "redirect:/accounts";
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public String handleAccountNotFoundException(AccountNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ExceptionHandler(InvalidAmountException.class)
    public String handleInvalidAmountException(InvalidAmountException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        return "error";
    }

    @GetMapping("/deposit/{accountNumber}")
    public String showDepositForm(@PathVariable String accountNumber, Model model) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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
        }catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "transaction/deposit";
        }
    }

    @GetMapping("/withdraw/{accountNumber}")
    public String showWithdrawForm(@PathVariable String accountNumber, Model model) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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
            model.addAttribute("message", "Withdraw successful. New balance: " + balance);
            return "withdraw/withdrawResult";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
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
    public String checkBalance(@RequestParam String accountNumber, Model model) {
        double balance = accountService.checkBalance(accountNumber);
        model.addAttribute("message", "Current balance: " + balance);
        return "balance/balanceResult";
    }

    @GetMapping("/transfer/{fromAccountNumber}")
    public String showTransferForm(@PathVariable String fromAccountNumber, Model model) {
        Account fromAccount = accountService.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

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
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "transaction/transfer";
        }
    }

    @GetMapping("/profile/{accountNumber}")
    public String showProfile(@PathVariable String accountNumber, Model model) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCreatedDate = account.getCreatedDate().format(formatter);

        model.addAttribute("account", account);
        model.addAttribute("formattedCreatedDate", formattedCreatedDate);

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

        List<Transaction> transactions = accountService.getAllTransactions();

        // Filter based on provided parameters
        if (fromDate != null && !fromDate.isEmpty()) {
            LocalDate from = LocalDate.parse(fromDate);
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(from.minusDays(1)))
                    .collect(Collectors.toList());
        }
        if (toDate != null && !toDate.isEmpty()) {
            LocalDate to = LocalDate.parse(toDate);
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(to.plusDays(1)))
                    .collect(Collectors.toList());
        }
        if (minAmount != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getAmount() >= minAmount)
                    .collect(Collectors.toList());
        }
        if (maxAmount != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getAmount() <= maxAmount)
                    .collect(Collectors.toList());
        }
        if (transactionType != null && !transactionType.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionType().equalsIgnoreCase(transactionType))
                    .collect(Collectors.toList());
        }

        model.addAttribute("transactions", transactions);
        List<String> transactionTypes = List.of("Deposit", "Withdrawal", "Transfer");
        model.addAttribute("transactionTypes", transactionTypes);

        return "transaction/viewTransactions";
    }
}
