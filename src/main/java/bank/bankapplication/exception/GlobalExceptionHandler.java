package bank.bankapplication.exception;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.security.auth.login.AccountNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAccountNotFoundException(AccountNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "common/error";
    }

    @ExceptionHandler(DuplicateAccountNumberException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateAccountNumberException(DuplicateAccountNumberException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "common/error";
    }

    @ExceptionHandler(InvalidAmountException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidAmountException(InvalidAmountException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "common/error";
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "common/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        return "common/error";
    }

}
