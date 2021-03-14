package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.Objects;

public final class NotExpression implements BooleanExpression {

    public static final int PRECEDENCE = 11;

    private final BooleanExpression expression;

    private NotExpression(BooleanExpression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    public static NotExpression of(BooleanExpression expression) {
        return new NotExpression(expression);
    }

    @Override
    public String print(PrintContext printContext) {
        return printContext.parenthesize(PRECEDENCE, "not " + expression.print(printContext.withPrecedence(PRECEDENCE)));
    }
}
