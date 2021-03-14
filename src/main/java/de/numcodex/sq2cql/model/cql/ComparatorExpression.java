package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.common.Comparator;

import java.util.Objects;

public final class ComparatorExpression implements BooleanExpression {

    private final Expression a;
    private final Comparator comparator;
    private final Expression b;

    private ComparatorExpression(Expression a, Comparator comparator, Expression b) {
        this.a = Objects.requireNonNull(a);
        this.comparator = Objects.requireNonNull(comparator);
        this.b = Objects.requireNonNull(b);
    }

    public static ComparatorExpression of(Expression a, Comparator comparator, Expression b) {
        return new ComparatorExpression(a, comparator, b);
    }

    @Override
    public String print(PrintContext printContext) {
        var precedence = comparator.getPrecedence();
        var childPrintContext = printContext.withPrecedence(precedence);
        return printContext.parenthesize(precedence, "%s %s %s".formatted(a.print(childPrintContext), comparator,
                b.print(childPrintContext)));
    }
}
