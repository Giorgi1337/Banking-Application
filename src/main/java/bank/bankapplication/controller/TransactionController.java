package bank.bankapplication.controller;


import bank.bankapplication.model.Transaction;
import bank.bankapplication.service.AccountService;
import bank.bankapplication.service.TransactionService;
import com.itextpdf.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class TransactionController extends BaseController {

    private final TransactionService transactionService;

    public TransactionController(AccountService accountService, TransactionService transactionService) {
        super(accountService, transactionService);
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public String viewAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Page<Transaction> transactionPage = transactionService.getTransactionsPaginated(page, size);

        addCommonModelAttributes(model, transactionPage, null);
        model.addAttribute("transactionTypes", List.of("Deposit", "Withdrawal", "Transfer"));
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("page", transactionPage);

        return "transaction/viewTransactions";
    }

    @GetMapping("/transactions/{accountHolderName}")
    public String viewTransactionsByHolder(@PathVariable String accountHolderName, Model model) {
        List<Transaction> transactions = transactionService.getTransactionsByAccountHolderName(accountHolderName);
        model.addAttribute("transactions", transactions);
        model.addAttribute("accountHolderName", accountHolderName);
        return "transaction/viewTransactionsByHolder";
    }

    @GetMapping("/transactions/{accountHolderName}/pdf")
    public ResponseEntity<InputStreamResource> downloadTransactionsPdfByHolder(
            @PathVariable String accountHolderName) throws DocumentException, IOException {

        return transactionService.generateTransactionsPdfByHolder(accountHolderName);
    }

    @GetMapping("/transactions/pdf")
    public ResponseEntity<InputStreamResource> downloadTransactionsPdf(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws DocumentException, IOException {

        return transactionService.generateTransactionsPdf(page, size);
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
        Page<Transaction> transactionPage = transactionService.searchTransactionsPaginated(from, to, minAmount, maxAmount, transactionType, page, size);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("transactionTypes", List.of("Deposit", "Withdrawal", "Transfer"));
        return "transaction/viewTransactions";
    }
}
