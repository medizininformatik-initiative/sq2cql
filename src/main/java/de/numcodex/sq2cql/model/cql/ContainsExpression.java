package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class ContainsExpression implements BooleanExpression {

    public static final int PRECEDENCE = 5;

    private final Expression a;
    private final Expression b;

    private ContainsExpression(Expression a, Expression b) {
        this.a = Objects.requireNonNull(a);
        this.b = b;
    }

    public static ContainsExpression of(Expression a, Expression b) {
        return new ContainsExpression(a, b);
    }

    @Override
    public String print(PrintContext printContext) {
        var childPrintContext = printContext.withPrecedence(PRECEDENCE);
        return printContext.parenthesize(PRECEDENCE, "%s contains %s".formatted(a.print(childPrintContext),
                b.print(childPrintContext)));
    }
}
