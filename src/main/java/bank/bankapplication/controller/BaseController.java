package bank.bankapplication.controller;


import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

public class BaseController {

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
}
