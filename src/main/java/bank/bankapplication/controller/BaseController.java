package bank.bankapplication.controller;


import bank.bankapplication.model.Account;
import bank.bankapplication.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;


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

    protected void populateAccountDetails(String accountNumber, Model model) {
        Account account = accountService.getAccountByNumber(accountNumber);
        model.addAttribute("accountNumber", account.getAccountNumber());
        model.addAttribute("accountHolderName", account.getAccountHolderName());
        model.addAttribute("balance", account.getBalance());
    }

}
