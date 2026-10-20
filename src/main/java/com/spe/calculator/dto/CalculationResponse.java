package com.spe.calculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CalculationResponse {
    private String operation;
    private Number a;
    private Number b;
    private Number result;

    public CalculationResponse() {
    }

    public CalculationResponse(String operation, double a, double b, double result) {
        this.operation = operation;
        this.a = formatNumber(a);
        this.b = formatNumber(b);
        this.result = formatNumber(result);
    }

    private static Number formatNumber(double val) {
        if (!Double.isInfinite(val) && !Double.isNaN(val) && val == Math.floor(val)) {
            return (long) val;
        }
        return val;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Number getA() {
        return a;
    }

    public void setA(Number a) {
        this.a = a;
    }

    public Number getB() {
        return b;
    }

    public void setB(Number b) {
        this.b = b;
    }

    public Number getResult() {
        return result;
    }

    public void setResult(Number result) {
        this.result = result;
    }

    @JsonIgnore
    public double getResultAsDouble() {
        return result != null ? result.doubleValue() : 0.0;
    }
}
