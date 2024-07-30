package bank.bankapplication.controller;

import bank.bankapplication.model.Account;
import bank.bankapplication.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        accountService.createAccount(
                account.getAccountNumber(),
                account.getAccountHolderName(),
                account.getBalance());
        return "redirect:/accounts";
    }

    @GetMapping("/deposit")
    public String showDepositForm(Model model) {
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
        }catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "depResult";
    }

    @GetMapping("/withdraw")
    public String showWithdrawForm() {
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
        }catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "withdrawResult";
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

    @GetMapping("/transfer")
    public String showTransferForm() {
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
        } catch (RuntimeException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
        }
        return "transferResult";
    }
}
