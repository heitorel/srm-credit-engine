package com.srm.creditengine.domain.shared;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FinancialMath {

    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 8;
    public static final int CALCULATION_SCALE = 24;
    private static final int MAX_ITERATIONS = 200;
    private static final BigDecimal EPSILON = new BigDecimal("0.000000000000000000000001");
    private static final MathContext MATH_CONTEXT = new MathContext(CALCULATION_SCALE, RoundingMode.HALF_UP);

    public BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal roundRate(BigDecimal value) {
        return value.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal roundTerm(BigDecimal value) {
        return value.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculatePresentValue(BigDecimal faceValue, BigDecimal baseRate, BigDecimal spread, BigDecimal term) {
        BigDecimal denominator = pow(BigDecimal.ONE.add(baseRate, MATH_CONTEXT).add(spread, MATH_CONTEXT), term);
        return faceValue.divide(denominator, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscount(BigDecimal faceValue, BigDecimal presentValue) {
        return faceValue.subtract(presentValue, MATH_CONTEXT);
    }

    public BigDecimal convert(BigDecimal amount, BigDecimal exchangeRate) {
        return amount.multiply(exchangeRate, MATH_CONTEXT);
    }

    public BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base must be greater than zero.");
        }
        return exp(exponent.multiply(ln(base), MATH_CONTEXT));
    }

    private BigDecimal ln(BigDecimal value) {
        BigDecimal z = value.subtract(BigDecimal.ONE, MATH_CONTEXT)
                .divide(value.add(BigDecimal.ONE, MATH_CONTEXT), MATH_CONTEXT);
        BigDecimal zSquared = z.multiply(z, MATH_CONTEXT);
        BigDecimal term = z;
        BigDecimal sum = term;

        for (int i = 1; i < MAX_ITERATIONS; i++) {
            term = term.multiply(zSquared, MATH_CONTEXT);
            BigDecimal fraction = term.divide(BigDecimal.valueOf((2L * i) + 1L), MATH_CONTEXT);
            sum = sum.add(fraction, MATH_CONTEXT);
            if (fraction.abs().compareTo(EPSILON) <= 0) {
                break;
            }
        }

        return sum.multiply(new BigDecimal("2"), MATH_CONTEXT);
    }

    private BigDecimal exp(BigDecimal value) {
        BigDecimal sum = BigDecimal.ONE;
        BigDecimal term = BigDecimal.ONE;

        for (int i = 1; i < MAX_ITERATIONS; i++) {
            term = term.multiply(value, MATH_CONTEXT)
                    .divide(BigDecimal.valueOf(i), MATH_CONTEXT);
            sum = sum.add(term, MATH_CONTEXT);
            if (term.abs().compareTo(EPSILON) <= 0) {
                break;
            }
        }

        return sum;
    }
}
