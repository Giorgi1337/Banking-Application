package bank.bankapplication.controller;

import bank.bankapplication.model.Account;
import bank.bankapplication.model.Transaction;
import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.service.AccountService;

import com.itextpdf.text.DocumentException;
import jakarta.validation.Valid;

import jakarta.validation.ValidationException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Controller
public class AccountController extends BaseController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        super(accountService);
        this.accountService = accountService;
    }

    @GetMapping("/accounts")
    public String viewAccountsPage(Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "") String name) {
        Page<Account> accountPage = accountService.getAccountsPaginated(page, size, name);
        addCommonModelAttributes(model, accountPage, name);
        model.addAttribute("accountPage", accountPage);
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
                    account.getAccountNumber(), account.getAccountHolderName(), account.getDateOfBirth(),
                    account.getEmailAddress(), account.getPhoneNumber(), account.getAddress(),
                    account.getAccountType(), account.getStatus()
            );
            return "redirect:/accounts";
        } catch (ValidationException e) {
            addErrorMessage(model, e.getMessage());
        } catch (Exception e) {
            addErrorMessage(model, "Unexpected error occurred: " + e.getMessage());
        }
        return "account/createAccount";
    }

    @GetMapping("/update/{accountNumber}")
    public String showUpdateForm(@PathVariable String accountNumber, Model model) throws AccountNotFoundException {
        Account account = accountService.getAccountByNumber(accountNumber);
        model.addAttribute("account", account);
        return "account/updateAccount";
    }

    @PostMapping("/update")
    public String updateAccount(@Valid @ModelAttribute Account account, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "account/updateAccount";
        }
        try {
            Account existingAccount = accountService.getAccountByNumber(account.getAccountNumber());
            updateAccountDetails(existingAccount, account);
            accountService.updateAccount(existingAccount);
            addSuccessMessage(model, "Account updated successfully");
            return "redirect:/accounts";
        } catch (AccountNotFoundException | RuntimeException e) {
            addErrorMessage(model, "Error updating account: " + e.getMessage());
            return "account/updateAccount";
        }
    }

    @GetMapping("/delete/{accountNumber}")
    public String deleteAccount(@PathVariable String accountNumber, Model model) {
        try {
            accountService.deleteAccount(accountNumber);
            return "redirect:/accounts";
        } catch (RuntimeException | AccountNotFoundException e) {
            addErrorMessage(model, "Error deleting account: " + e.getMessage());
            return "redirect:/accounts";
        }
    }

    @GetMapping("/deposit/{accountNumber}")
    public String showDepositForm(@PathVariable String accountNumber, Model model) throws AccountNotFoundException {
        populateAccountDetails(accountNumber, model);
        return "transaction/deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam String accountNumber, @RequestParam double amount, Model model) {
        try {
            double balance = accountService.deposit(accountNumber, amount);
            addSuccessMessage(model, "Deposit successful. New balance: " + balance);
            return "transaction/depResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            addErrorMessage(model, "Error during deposit: " + e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "transaction/deposit";
        }
    }

    @GetMapping("/withdraw/{accountNumber}")
    public String showWithdrawForm(@PathVariable String accountNumber, Model model) throws AccountNotFoundException {
        populateAccountDetails(accountNumber, model);
        return "withdraw/withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam String accountNumber, @RequestParam double amount, Model model) {
        try {
            double balance = accountService.withdraw(accountNumber, amount);
            addSuccessMessage(model, "Withdrawal successful. New balance: " + balance);
            return "withdraw/withdrawResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            addErrorMessage(model, "Error during withdrawal: " + e.getMessage());
            model.addAttribute("accountNumber", accountNumber);
            return "withdraw/withdraw";
        }
    }

    @GetMapping("/withdrawals")
    public String showWithdrawals(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Withdrawal> withdrawalPage = accountService.getWithdrawalsPaginated(page, size);
        addCommonModelAttributes(model, withdrawalPage, null);
        model.addAttribute("withdrawalPage", withdrawalPage);
        return "withdraw/withdrawals";
    }

    @GetMapping("/withdrawals/pdf")
    public ResponseEntity<InputStreamResource> downloadWithdrawalsPdf(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws DocumentException, IOException {

        return accountService.generateWithdrawalsPdf(page, size);
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
    public String showTransferForm(@PathVariable String fromAccountNumber, Model model) throws AccountNotFoundException {
        Account fromAccount = accountService.getAccountByNumber(fromAccountNumber);
        List<Account> accounts = accountService.getAllAccounts();
        accounts.remove(fromAccount);

        model.addAttribute("fromAccountNumber", fromAccountNumber);
        model.addAttribute("fromAccountHolderName", fromAccount.getAccountHolderName());
        model.addAttribute("fromAccountBalance", fromAccount.getBalance());
        model.addAttribute("accounts", accounts);

        return "transaction/transfer";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String fromAccountNumber, @RequestParam String toAccountNumber, @RequestParam double amount, Model model) {
        try {
            accountService.transferByAccountNumbers(fromAccountNumber, toAccountNumber, amount);
            addSuccessMessage(model, "Transfer successful");
            return "transaction/transferResult";
        } catch (RuntimeException | AccountNotFoundException e) {
            addErrorMessage(model, "Error during transfer: " + e.getMessage());
            return "transaction/transfer";
        }
    }

    @GetMapping("/profile/{accountNumber}")
    public String showProfile(@PathVariable String accountNumber, Model model) throws AccountNotFoundException {
        Account account = accountService.getAccountByNumber(accountNumber);
        LocalDateTime createdDateTime = account.getCreatedDate();
        String formattedCreatedDate = formatDate(createdDateTime);

        List<Transaction> recentTransactions = getRecentTransactions(account.getAccountHolderName());

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
    public String viewAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Transaction> transactionPage = accountService.getTransactionsPaginated(page, size);

        addCommonModelAttributes(model, transactionPage, null);
        model.addAttribute("transactionTypes", List.of("Deposit", "Withdrawal", "Transfer"));
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("page", transactionPage);

        return "transaction/viewTransactions";
    }

    @GetMapping("/transactions/{accountHolderName}/pdf")
    public ResponseEntity<InputStreamResource> downloadTransactionsPdfByHolder(
            @PathVariable String accountHolderName) throws DocumentException, IOException {

        return accountService.generateTransactionsPdfByHolder(accountHolderName);
    }

    @GetMapping("/transactions/pdf")
    public ResponseEntity<InputStreamResource> downloadTransactionsPdf(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws DocumentException, IOException {

        return accountService.generateTransactionsPdf(page, size);
    }


    @GetMapping("/transactions/search")
    public String searchTransactions(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String transactionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);
        Page<Transaction> transactionPage = accountService.searchTransactionsPaginated(from, to, minAmount, maxAmount, transactionType, page, size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("transactionTypes", List.of("Deposit", "Withdrawal", "Transfer"));
        return "transaction/viewTransactions";
    }

    @GetMapping("favicon.ico")
    public ResponseEntity<Void> favicon() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
