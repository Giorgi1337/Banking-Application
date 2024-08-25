        //package bank.bankapplication.controller;
        //
        //import bank.bankapplication.model.Account;
        //import bank.bankapplication.model.Transaction;
        //import bank.bankapplication.service.AccountService;
        //import com.itextpdf.text.DocumentException;
        //import org.junit.jupiter.api.BeforeEach;
        //import org.junit.jupiter.api.Test;
        //import org.mockito.InjectMocks;
        //import org.mockito.Mock;
        //import org.mockito.MockitoAnnotations;
        //import org.springframework.core.io.InputStreamResource;
        //import org.springframework.data.domain.Page;
        //import org.springframework.http.ResponseEntity;
        //import org.springframework.ui.Model;
        //import org.springframework.validation.BindingResult;
        //
        //import javax.security.auth.login.AccountNotFoundException;
        //import java.io.IOException;
        //import java.time.LocalDateTime;
        //import java.util.Collections;
        //import java.util.List;
        //
        //import static org.junit.jupiter.api.Assertions.assertEquals;
        //import static org.mockito.Mockito.*;
        //
        //public class AccountControllerTest {
        //
        //    @InjectMocks
        //    private AccountController accountController;
        //
        //    @Mock
        //    private AccountService accountService;
        //
        //    @Mock
        //    private Model model;
        //
        //    @Mock
        //    private BindingResult bindingResult;
        //
        //    @BeforeEach
        //    public void setup() {
        //        MockitoAnnotations.openMocks(this);
        //    }
        //
        //    @Test
        //    public void testViewAccountsPage() {
        //        Page<Account> accountPage = mock(Page.class);
        //        when(accountService.getAccountsPaginated(0, 10, "")).thenReturn(accountPage);
        //
        //        String viewName = accountController.viewAccountsPage(model, 0, 10, "");
        //        assertEquals("account/viewAccounts", viewName);
        //        verify(model).addAttribute("accountPage", accountPage);
        //    }
        //
        //    @Test
        //    public void testCreateAccount() {
        //        Account account = new Account();
        //        when(bindingResult.hasErrors()).thenReturn(false);
        //        doNothing().when(accountService).createAccount(any(), any(), any(), any(), any(), any(), any(), any());
        //
        //        String viewName = accountController.createAccount(account, bindingResult, model);
        //        assertEquals("redirect:/accounts", viewName);
        //    }
        //
        //    @Test
        //    public void testCreateAccountWithErrors() {
        //        Account account = new Account();
        //        when(bindingResult.hasErrors()).thenReturn(true);
        //
        //        String viewName = accountController.createAccount(account, bindingResult, model);
        //        assertEquals("account/createAccount", viewName);
        //    }
        //
        //    @Test
        //    public void testShowUpdateForm() throws AccountNotFoundException {
        //        Account account = new Account();
        //        when(accountService.getAccountByNumber(anyString())).thenReturn(account);
        //
        //        String viewName = accountController.showUpdateForm("12345", model);
        //        assertEquals("account/updateAccount", viewName);
        //        verify(model).addAttribute("account", account);
        //    }
        //
        //    @Test
        //    public void testUpdateAccount() throws AccountNotFoundException {
        //        // Create a mock account with required fields
        //        Account account = new Account();
        //        account.setAccountNumber("12345");
        //        account.setAccountHolderName("John Doe");
        //        account.setEmailAddress("john.doe@example.com");
        //        account.setPhoneNumber("123-456-7890");
        //        account.setAddress("123 Main St");
        //        account.setAccountType("Savings");
        //        account.setStatus("Active");
        //
        //        // Create an existing account with similar fields
        //        Account existingAccount = new Account();
        //        existingAccount.setAccountNumber("12345");
        //
        //        // Setup mocks
        //        when(bindingResult.hasErrors()).thenReturn(false); // Simulate no validation errors
        //        when(accountService.getAccountByNumber("12345")).thenReturn(existingAccount);
        //        doNothing().when(accountService).updateAccount(any(Account.class)); // Simulate successful update
        //
        //        // Perform the update account action
        //        String viewName = accountController.updateAccount(account, bindingResult, model);
        //
        //        // Verify the result
        //        assertEquals("redirect:/accounts", viewName);
        //
        //        // Verify that updateAccount was called with the expected argument
        //        verify(accountService).updateAccount(existingAccount);
        //
        //        // Check that the success message is added to the model with the correct attribute name
        //        verify(model).addAttribute("message", "Account updated successfully");
        //    }
        //
        //
        //
        //    @Test
        //    public void testUpdateAccountWithErrors() {
        //        Account account = new Account();
        //        when(bindingResult.hasErrors()).thenReturn(true);
        //
        //        String viewName = accountController.updateAccount(account, bindingResult, model);
        //        assertEquals("account/updateAccount", viewName);
        //    }
        //
        //    @Test
        //    public void testShowProfile() throws AccountNotFoundException {
        //        // Initialize the mock account
        //        Account account = new Account();
        //        account.setCreatedDate(LocalDateTime.now()); // Set a non-null value for createdDate
        //
        //        List<Transaction> transactions = Collections.emptyList();
        //
        //        when(accountService.getAccountByNumber(anyString())).thenReturn(account);
        //        when(accountService.getTransactionsByAccountHolderName(anyString())).thenReturn(transactions);
        //
        //        String viewName = accountController.showProfile("12345", model);
        //        assertEquals("account/profile", viewName);
        //
        //        // Verify that the createdDate is formatted and added to the model
        //        verify(model).addAttribute(eq("formattedCreatedDate"), anyString());
        //    }
        //
        //    @Test
        //    public void testViewTransactions() {
        //        Page<Transaction> transactionPage = mock(Page.class);
        //        when(accountService.getTransactionsPaginated(0, 10)).thenReturn(transactionPage);
        //
        //        String viewName = accountController.viewAllTransactions(0, 10, model);
        //        assertEquals("transaction/viewTransactions", viewName);
        //    }
        //
        //    @Test
        //    public void testDownloadTransactionsPdf() throws DocumentException, IOException {
        //        ResponseEntity<InputStreamResource> response = mock(ResponseEntity.class);
        //        when(accountService.generateTransactionsPdf(0, 10)).thenReturn(response);
        //
        //        ResponseEntity<InputStreamResource> result = accountController.downloadTransactionsPdf(0, 10);
        //        assertEquals(response, result);
        //    }
        //
        //    @Test
        //    public void testSearchTransactions() {
        //        Page<Transaction> transactionPage = mock(Page.class);
        //        when(accountService.searchTransactionsPaginated(null, null, null, null, null, 0, 10)).thenReturn(transactionPage);
        //
        //        String viewName = accountController.searchTransactions(null, null, null, null, null, 0, 10, model);
        //        assertEquals("transaction/viewTransactions", viewName);
        //    }
        //}
