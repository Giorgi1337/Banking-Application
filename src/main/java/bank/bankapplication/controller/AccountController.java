package bank.bankapplication.controller;

import bank.bankapplication.model.Account;
import bank.bankapplication.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "viewAccounts";
    }

    @GetMapping("/create")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("account", new Account());
        return "createAccount";
    }

    @PostMapping("/create")
    public String createAccount(@ModelAttribute Account account, Model model) {
        try {
            accountService.createAccount(
                    account.getAccountNumber(),
                    account.getAccountHolderName(),
                    account.getBalance());
            return "redirect:/accounts";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "createAccount";
        }
    }

    @GetMapping("/update/{accountNumber}")
    public String showUpdateForm(@PathVariable String accountNumber, Model model) {
        Account account = accountService.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        model.addAttribute("account", account);
        return "updateAccount";
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

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @GetMapping("/deposit/{accountNumber}")
    public String showDepositForm(@PathVariable String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(
            @RequestParam String accountNumber,
            @RequestParam double amount,
            Model model) {
        try {
            double balance = accountService.deposit(accountNumber, amount);
            model.addAttribute("message", "Deposit successful. New balance: " + balance);
            return "depResult";
        }catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "deposit";
        }
    }

    @GetMapping("/withdraw/{accountNumber}")
    public String showWithdrawForm(@PathVariable String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String accountNumber,
            @RequestParam double amount,
            Model model) {
        try {
            double balance = accountService.withdraw(accountNumber, amount);
            model.addAttribute("message", "Withdraw successful. New balance: " + balance);
            return "withdrawResult";
        }catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "error";
        }
    }

    @GetMapping("/balance")
    public String showBalanceForm() {
        return "checkBalance";
    }

    @PostMapping("/balance")
    public String checkBalance(@RequestParam String accountNumber, Model model) {
        double balance = accountService.checkBalance(accountNumber);
        model.addAttribute("message", "Current balance: " + balance);
        return "balanceResult";
    }

    @GetMapping("/transfer/{accountNumber}")
    public String showTransferForm(@PathVariable String accountNumber, Model model) {
        model.addAttribute("fromAccountNumber", accountNumber);
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transfer(
            @RequestParam String fromAccountNumber,
            @RequestParam String toAccountNumber,
            @RequestParam double amount, Model model) {
        try {
            accountService.transfer(fromAccountNumber, toAccountNumber, amount);
            model.addAttribute("message", "Transfer successful");
            return "transferResult";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("fromAccountNumber", fromAccountNumber);
            return "transfer";
        }
    }
}
