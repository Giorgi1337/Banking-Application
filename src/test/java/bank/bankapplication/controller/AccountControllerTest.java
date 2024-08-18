package bank.bankapplication.controller;

import bank.bankapplication.exception.DuplicateAccountNumberException;
import bank.bankapplication.model.Account;
import bank.bankapplication.service.AccountService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    public void testViewAccountsPage() throws Exception {

        int page = 0;
        int size = 10;
        String name = "";
        Page<Account> accountPage = new PageImpl<>(Collections.emptyList());

        when(accountService.getAccountsPaginated(page, size, name)).thenReturn(accountPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/accounts")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("name", name))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("account/viewAccounts"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("accountPage"))
                .andExpect(MockMvcResultMatchers.model().attribute("accountPage", accountPage));
    }

    @Test
    public void testShowCreateAccountForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/create"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("account/createAccount"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("account"))
                .andExpect(MockMvcResultMatchers.model().attribute("account", new Account()));
    }

    @Test
    public void testCreateAccount_DuplicateAccountNumber() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setAccountHolderName("John Doe");
        account.setDateOfBirth(LocalDate.parse("1990-01-01"));

        doThrow(new DuplicateAccountNumberException("Account number already exists"))
                .when(accountService).createAccount(account.getAccountNumber(), account.getAccountHolderName(), account.getDateOfBirth());

        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                        .param("accountNumber", account.getAccountNumber())
                        .param("accountHolderName", account.getAccountHolderName())
                        .param("dateOfBirth", String.valueOf(account.getDateOfBirth())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("account/createAccount"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Account number already exists"));
    }

    @Test
    public void testCreateAccount_Exception() throws Exception {
        Account account = new Account();
        account.setAccountNumber("123456");
        account.setAccountHolderName("John Doe");
        account.setDateOfBirth(LocalDate.parse("1990-01-01"));

        doThrow(new RuntimeException("Unexpected error"))
                .when(accountService).createAccount(account.getAccountNumber(), account.getAccountHolderName(), account.getDateOfBirth());

        mockMvc.perform(MockMvcRequestBuilders.post("/create")
                        .param("accountNumber", account.getAccountNumber())
                        .param("accountHolderName", account.getAccountHolderName())
                        .param("dateOfBirth", String.valueOf(account.getDateOfBirth())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("account/createAccount"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
                .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Unexpected error occurred: Unexpected error"));
    }

    @Test
    public void testShowUpdateForm() throws Exception {
        String accountNumber = "123456";
        Account account = new Account();
        account.setAccountNumber(accountNumber);

        when(accountService.getAccountByNumber(accountNumber)).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders.get("/update/{accountNumber}", accountNumber))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("account/updateAccount"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("account"))
                .andExpect(MockMvcResultMatchers.model().attribute("account", account));
    }
}
