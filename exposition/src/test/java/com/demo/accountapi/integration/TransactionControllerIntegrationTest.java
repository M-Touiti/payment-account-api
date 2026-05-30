package com.demo.accountapi.integration;

import com.demo.accountapi.application.dto.request.CreateAccountRequest;
import com.demo.accountapi.application.dto.request.CreateTransactionRequest;
import com.demo.accountapi.application.dto.request.LoginRequest;
import com.demo.accountapi.application.dto.request.RegisterRequest;
import com.demo.accountapi.domain.model.AccountType;
import com.demo.accountapi.domain.model.TransactionType;
import com.demo.accountapi.exposition.PaymentAccountApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentAccountApplication.class)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TransactionControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_account_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String userToken;
    private String accountId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.com";
        userToken = registerAndLogin(email, "password123");
        accountId = createAccount(userToken);
    }

    @Test
    void shouldCreditAccount() throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                TransactionType.CREDIT, new BigDecimal("300.00"), "Salary");

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.balanceAfter").value(300.00));
    }

    @Test
    void shouldDebitAccount() throws Exception {
        credit(accountId, userToken, new BigDecimal("500.00"));

        CreateTransactionRequest debit = new CreateTransactionRequest(
                TransactionType.DEBIT, new BigDecimal("200.00"), "Rent");

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEBIT"))
                .andExpect(jsonPath("$.balanceAfter").value(300.00));
    }

    @Test
    void shouldReturn422OnInsufficientFunds() throws Exception {
        CreateTransactionRequest debit = new CreateTransactionRequest(
                TransactionType.DEBIT, new BigDecimal("999.00"), "Overdraft");

        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debit)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldListAllTransactions() throws Exception {
        credit(accountId, userToken, new BigDecimal("100.00"));
        credit(accountId, userToken, new BigDecimal("50.00"));

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldListTransactionsByType() throws Exception {
        credit(accountId, userToken, new BigDecimal("100.00"));
        credit(accountId, userToken, new BigDecimal("200.00"));

        mockMvc.perform(get("/api/v1/accounts/" + accountId + "/transactions")
                        .param("type", "CREDIT")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].type").value("CREDIT"));
    }

    // --- helpers ---

    private String registerAndLogin(String email, String password) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(email, password))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String createAccount(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAccountRequest(AccountType.CHECKING, "EUR"))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }

    private void credit(String accountId, String token, BigDecimal amount) throws Exception {
        mockMvc.perform(post("/api/v1/accounts/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTransactionRequest(
                                TransactionType.CREDIT, amount, "Setup credit"))))
                .andExpect(status().isCreated());
    }
}
