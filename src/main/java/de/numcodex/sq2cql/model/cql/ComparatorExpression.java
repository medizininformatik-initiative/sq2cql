package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.common.Comparator;

import static java.util.Objects.requireNonNull;

public record ComparatorExpression(Expression a, Comparator comparator, Expression b) implements BooleanExpression {

    public ComparatorExpression {
        requireNonNull(a);
        requireNonNull(comparator);
        requireNonNull(b);
    }

    public static ComparatorExpression of(Expression a, Comparator comparator, Expression b) {
        return new ComparatorExpression(a, comparator, b);
    }

    public static ComparatorExpression equal(Expression a, Expression b) {
        return new ComparatorExpression(a, Comparator.EQUAL, b);
    }

    @Override
    public String print(PrintContext printContext) {
        var precedence = comparator.getPrecedence();
        var childPrintContext = printContext.withPrecedence(precedence);
        return printContext.parenthesize(precedence, "%s %s %s".formatted(a.print(childPrintContext), comparator,
                b.print(childPrintContext)));
    }
}
