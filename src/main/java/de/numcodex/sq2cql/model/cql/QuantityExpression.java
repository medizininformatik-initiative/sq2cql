package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.math.BigDecimal;
import java.util.Objects;

public final class QuantityExpression implements Expression {

    private final BigDecimal value;
    private final String unit;

    private QuantityExpression(BigDecimal value, String unit) {
        this.value = Objects.requireNonNull(value);
        this.unit = unit;
    }

    public static QuantityExpression of(BigDecimal value) {
        return new QuantityExpression(value, null);
    }

    public static QuantityExpression of(BigDecimal value, String unit) {
        return new QuantityExpression(value, Objects.requireNonNull(unit));
    }

    @Override
    public String print(PrintContext printContext) {
        return unit == null ? value.toString() : "%s '%s'".formatted(value, unit);
    }
}
