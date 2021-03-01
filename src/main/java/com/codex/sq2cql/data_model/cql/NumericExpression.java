package com.codex.sq2cql.data_model.cql;

import java.util.Optional;

public class NumericExpression implements Expression {
    private final double value;
    private String unit;


    public NumericExpression(double value) {
        this.value = value;
    }

    public NumericExpression(double value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return format(value) + (unit == null ? "" : " '%s'".formatted(unit));
    }

    public static String format(double d)
    {
        //Remove Trailing Zeros if value is an integer
        if(d == (long) d) {
            return String.format("%d", (long) d);
        }
        else {
            return String.format("%s", d);
        }
    }
}
