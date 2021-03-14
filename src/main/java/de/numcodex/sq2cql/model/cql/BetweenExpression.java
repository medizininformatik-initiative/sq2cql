package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class BetweenExpression implements BooleanExpression {

    public static final int PRECEDENCE = 10;

    private final Expression value;
    private final Expression lowerBound;
    private final Expression upperBound;

    private BetweenExpression(Expression value, Expression lowerBound, Expression upperBound) {
        this.value = Objects.requireNonNull(value);
        this.lowerBound = Objects.requireNonNull(lowerBound);
        this.upperBound = Objects.requireNonNull(upperBound);
    }

    public static BetweenExpression of(Expression value, Expression lowerBound, Expression upperBound) {
        return new BetweenExpression(value, lowerBound, upperBound);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s between %s and %s".formatted(value.print(childPrintContext),
                lowerBound.print(childPrintContext), upperBound.print(childPrintContext)));
    }
}
