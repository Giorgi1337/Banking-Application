package bank.bankapplication.controller;

import bank.bankapplication.exception.AccountNotFoundException;
import bank.bankapplication.exception.InvalidAmountException;
import bank.bankapplication.exception.ValidationException;
import bank.bankapplication.model.Account;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;


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
        model.addAttribute("accountNumber", accountNumber);
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
        model.addAttribute("account", account);
        return "account/profile";
    }
}
