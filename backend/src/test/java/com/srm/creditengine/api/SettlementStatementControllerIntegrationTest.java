package com.srm.creditengine.api;

import com.srm.creditengine.integration.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnabledIf("isDockerAvailable")
class SettlementStatementControllerIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        jdbcTemplate.update("DELETE FROM settlement_items");
        jdbcTemplate.update("DELETE FROM settlements");
        jdbcTemplate.update("DELETE FROM receivables");
        jdbcTemplate.update("DELETE FROM assignors");
        jdbcTemplate.update("DELETE FROM exchange_rates");
        seedSettlements();
    }

    @Test
    void shouldReturnDefaultFirstPage() throws Exception {
        mockMvc.perform(get("/api/settlements/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].settlementId").value("30000000-0000-0000-0000-000000000003"))
                .andExpect(jsonPath("$.content[1].settlementId").value("10000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.content[2].settlementId").value("20000000-0000-0000-0000-000000000002"));
    }

    @Test
    void shouldReturnRequestedPageAndSize() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?page=1&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.content[0].settlementId").value("10000000-0000-0000-0000-000000000001"));
    }

    @Test
    void shouldRejectNegativePage() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("page"));
    }

    @Test
    void shouldRejectZeroSize() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("size"));
    }

    @Test
    void shouldRejectPageSizeAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid pagination parameters."))
                .andExpect(jsonPath("$.details[0].field").value("size"));
    }

    @Test
    void shouldSortBySettledAtDescendingByDefaultWithDeterministicTieBreaker() throws Exception {
        mockMvc.perform(get("/api/settlements/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[1].settlementId").value("10000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.content[2].settlementId").value("20000000-0000-0000-0000-000000000002"));
    }

    @Test
    void shouldReturnAscendingSortWhenRequested() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?sort=settledAt,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].settlementId").value("10000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.content[1].settlementId").value("20000000-0000-0000-0000-000000000002"))
                .andExpect(jsonPath("$.content[2].settlementId").value("30000000-0000-0000-0000-000000000003"));
    }

    @Test
    void shouldRejectUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?sort=status,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported sort field: status."));
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?from=2026-07-08&to=2026-07-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldRejectInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?from=2026-07-09&to=2026-07-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid statement date range."))
                .andExpect(jsonPath("$.details[0].field").value("from"));
    }

    @Test
    void shouldFilterByAssignorId() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?assignorId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterByAssignorDocument() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?assignorDocument=12345678000199"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void shouldFilterByPaymentCurrency() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?paymentCurrency=USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].paymentCurrency").value("USD"));
    }

    @Test
    void shouldFilterBySourceCurrency() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?sourceCurrency=USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sourceCurrency").value("USD"));
    }

    @Test
    void shouldFilterByReceivableType() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?receivableType=POST_DATED_CHECK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].settlementId").value("20000000-0000-0000-0000-000000000002"));
    }

    @Test
    void shouldFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?status=CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"));
    }

    @Test
    void shouldCombineMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?assignorDocument=12345678000199&paymentCurrency=USD&status=SETTLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].settlementId").value("10000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.content[0].totalPaymentValue").value(1808.45));
    }

    @Test
    void shouldReturnEmptyPageWhenNoRowsMatch() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?assignorDocument=00000000000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnStructuredErrorForUnsupportedCurrency() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?paymentCurrency=EUR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported currency: EUR."))
                .andExpect(jsonPath("$.details[0].field").value("paymentCurrency"));
    }

    @Test
    void shouldReturnStructuredErrorForUnsupportedReceivableType() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?receivableType=INVOICE"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Unsupported receivable type: INVOICE."))
                .andExpect(jsonPath("$.details[0].field").value("receivableType"));
    }

    @Test
    void shouldReturnStructuredErrorForUnsupportedStatus() throws Exception {
        mockMvc.perform(get("/api/settlements/statement?status=ARCHIVED"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Unsupported settlement status: ARCHIVED."))
                .andExpect(jsonPath("$.details[0].field").value("status"));
    }

    private void seedSettlements() {
        jdbcTemplate.update("""
                INSERT INTO assignors (id, name, document, created_at, updated_at) VALUES
                ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACME Comercio Ltda.', '12345678000199', '2026-07-07 10:00:00', '2026-07-07 10:00:00'),
                ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Globex SA', '99887766000155', '2026-07-07 10:00:00', '2026-07-07 10:00:00')
                """);

        jdbcTemplate.update("""
                INSERT INTO settlements (
                    id, assignor_id, source_currency_code, payment_currency_code, status, base_rate, item_count,
                    total_face_value, total_present_value, total_payment_value, settled_at, created_at, updated_at
                ) VALUES
                ('10000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'BRL', 'USD', 'SETTLED', 0.01000000, 1, 10000.0000, 9518.1400, 1808.4500, '2026-07-08 12:00:00', '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('20000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'BRL', 'BRL', 'CANCELLED', 0.01000000, 1, 5000.0000, 4380.1200, 4380.1200, '2026-07-08 12:00:00', '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('30000000-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'USD', 'USD', 'SETTLED', 0.01000000, 1, 7000.0000, 6650.5500, 6650.5500, '2026-07-09 12:00:00', '2026-07-09 12:00:00', '2026-07-09 12:00:00')
                """);

        jdbcTemplate.update("""
                INSERT INTO receivables (
                    id, assignor_id, external_reference, receivable_type_code, face_value, currency_code, due_date,
                    status, version, created_at, updated_at
                ) VALUES
                ('a1000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'NF-1001', 'MERCANTILE_DUPLICATE', 10000.0000, 'BRL', '2026-09-05', 'SETTLED', 1, '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('a2000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'CHK-2001', 'POST_DATED_CHECK', 5000.0000, 'BRL', '2026-10-15', 'SETTLED', 1, '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('a3000000-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'USD-3001', 'MERCANTILE_DUPLICATE', 7000.0000, 'USD', '2026-09-20', 'SETTLED', 1, '2026-07-09 12:00:00', '2026-07-09 12:00:00')
                """);

        jdbcTemplate.update("""
                INSERT INTO settlement_items (
                    id, settlement_id, receivable_id, external_reference, receivable_type_code, face_value,
                    source_currency_code, payment_currency_code, base_rate, spread, term_in_months,
                    present_value_source, discount_value, payment_value, exchange_rate, calculated_at, created_at
                ) VALUES
                ('b1000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'a1000000-0000-0000-0000-000000000001', 'NF-1001', 'MERCANTILE_DUPLICATE', 10000.0000, 'BRL', 'USD', 0.01000000, 0.01500000, 2.00000000, 9518.1400, 481.8600, 1808.4500, 0.19000000, '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('b2000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', 'a2000000-0000-0000-0000-000000000002', 'CHK-2001', 'POST_DATED_CHECK', 5000.0000, 'BRL', 'BRL', 0.01000000, 0.02500000, 3.00000000, 4380.1200, 619.8800, 4380.1200, NULL, '2026-07-08 12:00:00', '2026-07-08 12:00:00'),
                ('b3000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000003', 'a3000000-0000-0000-0000-000000000003', 'USD-3001', 'MERCANTILE_DUPLICATE', 7000.0000, 'USD', 'USD', 0.01000000, 0.01500000, 2.00000000, 6650.5500, 349.4500, 6650.5500, NULL, '2026-07-09 12:00:00', '2026-07-09 12:00:00')
                """);
    }
}
