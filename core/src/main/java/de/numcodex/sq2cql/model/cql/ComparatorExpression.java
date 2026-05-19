package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.common.Comparator;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public record ComparatorExpression(Expression<?> a, Comparator comparator,
                                   Expression<?> b) implements DefaultExpression {

    public ComparatorExpression {
        requireNonNull(a);
        requireNonNull(comparator);
        requireNonNull(b);
    }

    public static ComparatorExpression of(Expression<?> a, Comparator comparator, Expression<?> b) {
        return new ComparatorExpression(a, comparator, b);
    }

    public static ComparatorExpression equal(Expression<?> a, Expression<?> b) {
        return new ComparatorExpression(a, Comparator.EQUAL, b);
    }

    public static ComparatorExpression equivalent(Expression<?> a, Expression<?> b) {
        return new ComparatorExpression(a, Comparator.EQUIVALENT, b);
    }

    @Override
    public String print(PrintContext printContext) {
        var precedence = comparator.getPrecedence();
        var childPrintContext = printContext.withPrecedence(precedence);
        return printContext.parenthesize(precedence, "%s %s %s".formatted(a.print(childPrintContext), comparator,
                b.print(childPrintContext)));
    }

    @Override
    public DefaultExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        return new ComparatorExpression(a.withIncrementedSuffixes(increments), comparator,
                b.withIncrementedSuffixes(increments));
    }
}
