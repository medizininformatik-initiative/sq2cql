package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class ExistsExpression implements BooleanExpression {

    public static final int PRECEDENCE = 11;

    private final Expression expression;

    private ExistsExpression(Expression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    public static ExistsExpression of(Expression expression) {
        return new ExistsExpression(expression);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "exists " + expression.print(printContext
                .withPrecedence(PRECEDENCE)));
    }
}
