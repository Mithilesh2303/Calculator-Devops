package com.spe.calculator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CalculatorController Spring MVC Integration Tests")
class CalculatorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/add?a=2&b=3 returns 200 and correct JSON")
    void testAddEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                .param("a", "2")
                .param("b", "3")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("add"))
                .andExpect(jsonPath("$.a").value(2))
                .andExpect(jsonPath("$.b").value(3))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/subtract?a=10&b=4 returns 200 and correct JSON")
    void testSubtractEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/subtract")
                .param("a", "10")
                .param("b", "4")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("subtract"))
                .andExpect(jsonPath("$.a").value(10))
                .andExpect(jsonPath("$.b").value(4))
                .andExpect(jsonPath("$.result").value(6));
    }

    @Test
    @DisplayName("GET /api/v1/multiply?a=5&b=6 returns 200 and correct JSON")
    void testMultiplyEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/multiply")
                .param("a", "5")
                .param("b", "6")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("multiply"))
                .andExpect(jsonPath("$.a").value(5))
                .andExpect(jsonPath("$.b").value(6))
                .andExpect(jsonPath("$.result").value(30));
    }

    @Test
    @DisplayName("GET /api/v1/divide?a=10&b=2 returns 200 and correct JSON")
    void testDivideEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/divide")
                .param("a", "10")
                .param("b", "2")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.operation").value("divide"))
                .andExpect(jsonPath("$.a").value(10))
                .andExpect(jsonPath("$.b").value(2))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/divide?a=10&b=0 returns 400 Bad Request with structured error JSON")
    void testDivideByZeroReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/divide")
                .param("a", "10")
                .param("b", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Division by zero is not allowed."))
                .andExpect(jsonPath("$.path").value("/api/v1/divide"));
    }

    @Test
    @DisplayName("GET /api/v1/add missing parameter 'b' returns 400 Bad Request")
    void testMissingParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                .param("a", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/add"));
    }

    @Test
    @DisplayName("GET /api/v1/add with invalid parameter returns 400 Bad Request")
    void testInvalidParameterReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/add")
                .param("a", "invalid")
                .param("b", "5")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/v1/add"));
    }

    @Test
    @DisplayName("GET /actuator/health returns 200 UP status")
    void testActuatorHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
