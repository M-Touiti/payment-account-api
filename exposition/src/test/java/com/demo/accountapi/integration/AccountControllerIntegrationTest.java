package com.demo.accountapi.integration;

import com.demo.accountapi.application.dto.request.CreateAccountRequest;
import com.demo.accountapi.application.dto.request.LoginRequest;
import com.demo.accountapi.application.dto.request.RegisterRequest;
import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.domain.model.AccountType;
import com.demo.accountapi.domain.model.Role;
import com.demo.accountapi.domain.model.User;
import com.demo.accountapi.exposition.PaymentAccountApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentAccountApplication.class)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AccountControllerIntegrationTest {

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
    @Autowired private UserRepositoryPort userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private String userEmail;
    private String adminEmail;

    @BeforeEach
    void setUp() throws Exception {
        userEmail = "user-" + UUID.randomUUID() + "@test.com";
        adminEmail = "admin-" + UUID.randomUUID() + "@test.com";
        userToken = registerAndLogin(userEmail, "password123");
        adminToken = createAdminAndLogin(adminEmail, "password123");
    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest(AccountType.CHECKING, "EUR"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetMyAccountsPaginated() throws Exception {
        createAccount(userToken, AccountType.CHECKING, "EUR");
        createAccount(userToken, AccountType.SAVINGS, "USD");

        mockMvc.perform(get("/api/v1/accounts/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetAccountByIdAsOwner() throws Exception {
        String accountId = createAccount(userToken, AccountType.SAVINGS, "USD");

        mockMvc.perform(get("/api/v1/accounts/" + accountId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId));
    }

    @Test
    void shouldReturn403WhenAccessingAnotherUsersAccount() throws Exception {
        String otherEmail = "other-" + UUID.randomUUID() + "@test.com";
        String otherToken = registerAndLogin(otherEmail, "password123");
        String otherAccountId = createAccount(otherToken, AccountType.CHECKING, "EUR");

        mockMvc.perform(get("/api/v1/accounts/" + otherAccountId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSuspendAccount() throws Exception {
        String accountId = createAccount(userToken, AccountType.CHECKING, "EUR");

        mockMvc.perform(patch("/api/v1/accounts/" + accountId + "/suspend")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void shouldReturn403WhenNonAdminClosesAccount() throws Exception {
        String accountId = createAccount(userToken, AccountType.CHECKING, "EUR");

        mockMvc.perform(delete("/api/v1/accounts/" + accountId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCloseAccountAsAdmin() throws Exception {
        String accountId = createAccount(userToken, AccountType.CHECKING, "EUR");

        mockMvc.perform(delete("/api/v1/accounts/" + accountId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn403WhenUserRequestsAllAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAllAccountsAsAdmin() throws Exception {
        createAccount(userToken, AccountType.CHECKING, "EUR");

        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
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

    private String createAdminAndLogin(String email, String password) throws Exception {
        userRepository.save(User.createNew(email, passwordEncoder.encode(password), Role.ADMIN));

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String createAccount(String token, AccountType type, String currency) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest(type, currency))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();
    }
}
