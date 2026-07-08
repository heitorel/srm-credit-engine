package com.srm.creditengine.api;

import com.srm.creditengine.integration.AbstractMySqlIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnabledIf("isDockerAvailable")
class ReferenceDataControllerIntegrationTest extends AbstractMySqlIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldListSupportedCurrencies() throws Exception {
        mockMvc.perform(get("/api/reference-data/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currencies[0].code").value("BRL"))
                .andExpect(jsonPath("$.currencies[0].name").value("Brazilian Real"))
                .andExpect(jsonPath("$.currencies[0].decimalPlaces").value(2))
                .andExpect(jsonPath("$.currencies[1].code").value("USD"))
                .andExpect(jsonPath("$.currencies[1].name").value("US Dollar"))
                .andExpect(jsonPath("$.currencies[1].decimalPlaces").value(2));
    }

    @Test
    void shouldListReceivableTypesWithMonthlySpreads() throws Exception {
        mockMvc.perform(get("/api/reference-data/receivable-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivableTypes[0].code").value("MERCANTILE_DUPLICATE"))
                .andExpect(jsonPath("$.receivableTypes[0].description").value("Mercantile Duplicate"))
                .andExpect(jsonPath("$.receivableTypes[0].monthlySpread").value(0.01500000))
                .andExpect(jsonPath("$.receivableTypes[1].code").value("POST_DATED_CHECK"))
                .andExpect(jsonPath("$.receivableTypes[1].description").value("Post-Dated Check"))
                .andExpect(jsonPath("$.receivableTypes[1].monthlySpread").value(0.02500000));
    }
}
