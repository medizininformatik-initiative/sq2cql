package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record OverlapsIntervalOperatorPhrase(Expression<?> leftInterval, Expression<?> rightInterval) implements
        DefaultExpression {

    public static final int PRECEDENCE = 7;

    public OverlapsIntervalOperatorPhrase {
        requireNonNull(leftInterval);
        requireNonNull(rightInterval);
    }

    public static DefaultExpression of(Expression<?> leftInterval, Expression<?> rightInterval) {
        return new OverlapsIntervalOperatorPhrase(leftInterval, rightInterval);
    }

    @Override
    public String print(PrintContext printContext) {
        var operatorPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s overlaps %s".formatted(leftInterval.print(operatorPrintContext),
                rightInterval.print(operatorPrintContext)));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new OverlapsIntervalOperatorPhrase(leftInterval.withIncrementedSuffixes(increments),
                rightInterval.withIncrementedSuffixes(increments));
    }
}
