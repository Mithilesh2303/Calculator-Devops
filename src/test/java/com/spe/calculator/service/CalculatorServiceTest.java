package com.spe.calculator.service;

import com.spe.calculator.exception.DivisionByZeroException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CalculatorService Unit Tests")
class CalculatorServiceTest {

    private CalculatorService calculatorService;

    @BeforeEach
    void setUp() {
        calculatorService = new CalculatorService();
    }

    @Test
    @DisplayName("add: should correctly add two positive numbers")
    void testAddPositiveNumbers() {
        assertEquals(5.0, calculatorService.add(2.0, 3.0));
    }

    @Test
    @DisplayName("add: should correctly add positive and negative numbers")
    void testAddWithNegativeNumbers() {
        assertEquals(-1.0, calculatorService.add(2.0, -3.0));
        assertEquals(0.0, calculatorService.add(-5.0, 5.0));
    }

    @Test
    @DisplayName("add: should correctly add floating point numbers")
    void testAddFloatingNumbers() {
        assertEquals(5.75, calculatorService.add(2.25, 3.50));
    }

    @Test
    @DisplayName("subtract: should correctly subtract two numbers")
    void testSubtractNumbers() {
        assertEquals(-1.0, calculatorService.subtract(2.0, 3.0));
        assertEquals(7.0, calculatorService.subtract(10.0, 3.0));
        assertEquals(13.0, calculatorService.subtract(10.0, -3.0));
    }

    @Test
    @DisplayName("multiply: should correctly multiply numbers")
    void testMultiplyNumbers() {
        assertEquals(6.0, calculatorService.multiply(2.0, 3.0));
        assertEquals(-6.0, calculatorService.multiply(2.0, -3.0));
        assertEquals(0.0, calculatorService.multiply(2.0, 0.0));
    }

    @Test
    @DisplayName("divide: should correctly divide numbers")
    void testDivideNumbers() {
        assertEquals(2.0, calculatorService.divide(6.0, 3.0));
        assertEquals(2.5, calculatorService.divide(5.0, 2.0));
        assertEquals(-2.0, calculatorService.divide(-6.0, 3.0));
    }

    @Test
    @DisplayName("divide: should throw DivisionByZeroException when divisor is 0")
    void testDivideByZeroThrowsException() {
        DivisionByZeroException exception = assertThrows(
                DivisionByZeroException.class,
                () -> calculatorService.divide(10.0, 0.0)
        );
        assertEquals("Division by zero is not allowed.", exception.getMessage());
    }
}
