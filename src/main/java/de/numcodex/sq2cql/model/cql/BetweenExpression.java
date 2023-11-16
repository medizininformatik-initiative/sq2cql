package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record BetweenExpression(Expression<?> value,
                                Expression<?> lowerBound,
                                Expression<?> upperBound) implements DefaultExpression {

    public static final int PRECEDENCE = 10;

    public BetweenExpression {
        requireNonNull(value);
        requireNonNull(lowerBound);
        requireNonNull(upperBound);
    }

    public static BetweenExpression of(Expression<?> value, Expression<?> lowerBound, Expression<?> upperBound) {
        return new BetweenExpression(value, lowerBound, upperBound);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s between %s and %s".formatted(value.print(childPrintContext),
                lowerBound.print(childPrintContext), upperBound.print(childPrintContext)));
    }
}
