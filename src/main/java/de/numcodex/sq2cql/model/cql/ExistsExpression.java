package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import static java.util.Objects.requireNonNull;

public record ExistsExpression(Expression expression) implements BooleanExpression {

    public static final int PRECEDENCE = 11;

    public ExistsExpression {
        requireNonNull(expression);
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
