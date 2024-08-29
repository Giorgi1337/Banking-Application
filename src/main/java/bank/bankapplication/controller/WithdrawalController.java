package bank.bankapplication.controller;


import bank.bankapplication.model.Withdrawal;
import bank.bankapplication.service.AccountService;
import bank.bankapplication.service.TransactionService;
import bank.bankapplication.service.WithdrawalService;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;

@Controller
public class WithdrawalController extends BaseController {

    private final WithdrawalService withdrawalService;
    private final AccountService accountService;

    public WithdrawalController(AccountService accountService, TransactionService transactionService, WithdrawalService withdrawalService, AccountService accountService1) {
        super(accountService, transactionService);
        this.withdrawalService = withdrawalService;
        this.accountService = accountService1;
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
        Page<Withdrawal> withdrawalPage = withdrawalService.getWithdrawalsPaginated(page, size);
        addCommonModelAttributes(model, withdrawalPage, null);
        model.addAttribute("withdrawalPage", withdrawalPage);
        return "withdraw/withdrawals";
    }

    @GetMapping("/withdrawals/pdf")
    public ResponseEntity<InputStreamResource> downloadWithdrawalsPdf(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws DocumentException, IOException {

        return withdrawalService.generateWithdrawalsPdf(page, size);
    }
}
